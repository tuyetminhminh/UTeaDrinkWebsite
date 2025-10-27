package net.codejava.utea.order.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.promotion.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;               // giỏ hàng theo User
    private final OrderRepository orderRepo;
    private final PromotionService promotionService;     // áp dụng voucher/promotion

    @Override
    public Order createFromCart(User user,
                                Consumer<Order.OrderBuilder> extra,
                                CheckoutRequest req) {

        // 1) lấy item đã chọn
        List<CartItem> selected = cartService.listSelected(user);
        if (selected == null || selected.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống (chưa chọn sản phẩm).");
        }

        // 2) subtotal + ship
        BigDecimal subtotal = selected.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = cartService.estimateShippingFee(subtotal);

        // 3a) Áp dụng voucher (mã phải nhập) - xử lý cả couponCode và shipCode
        var voucherResult = promotionService.applyBoth(req.getCouponCode(), req.getShipCode(), subtotal, shippingFee, user);
        
        // 3b) Áp dụng chương trình khuyến mãi tự động
        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, user);
        
        // 3c) Logic ưu tiên: MÃ GIẢM GIÁ trước, KHUYẾN MÃI sau (KHÔNG CỘNG DỒN)
        // - Giảm giá sản phẩm: Dùng voucher HOẶC promotion (ưu tiên voucher)
        // - Freeship: Dùng voucher HOẶC promotion (ưu tiên voucher)
        BigDecimal productDiscount = voucherResult.okDiscount() 
                ? voucherResult.discount()           // Có voucher giảm giá → dùng voucher
                : autoPromos.discount().discount();  // Không có voucher → dùng promotion
        
        BigDecimal freeshipDiscount = voucherResult.okShip() 
                ? voucherResult.shipDiscount()       // Có voucher freeship → dùng voucher
                : autoPromos.freeship().discount();  // Không có voucher → dùng promotion freeship
        
        BigDecimal discount = productDiscount.add(freeshipDiscount);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        // 4) build Order theo entity thực tế
        // LƯU Ý: Order bắt buộc có shop -> giả định giỏ hàng 1 shop; lấy từ item đầu tiên
        var firstShop = selected.get(0).getProduct().getShop();

        // Gộp cả 2 mã voucher (nếu có) để lưu vào voucherCode
        String voucherCodes = buildVoucherCodesString(req.getCouponCode(), req.getShipCode());

        Order.OrderBuilder ob = Order.builder()
                .orderCode(genOrderCode())
                .user(user)
                .shop(firstShop)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                // shippingAddress: nếu bạn có logic snapshot Address, set vào đây; hiện để null
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .voucherCode(voucherCodes);

        if (extra != null) extra.accept(ob);
        Order order = ob.build();

        // 5) order items – KHỚP đúng OrderItem entity (không set productName/imageUrl)
        List<OrderItem> items = new ArrayList<>(selected.size());
        for (CartItem ci : selected) {
            items.add(OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .variant(ci.getVariant()) // có thể null
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .lineTotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    // toppingsJson, note: nếu bạn lưu ở CartItem thì set thêm; hiện để null
                    .build());
        }
        order.setItems(items);

        // 6) lưu & dọn cart (chỉ xóa item đã chọn)
        Order saved = orderRepo.save(order);
        //elected.forEach(i -> cartService.removeItem(user, i.getId()));

        // 7) Cập nhật lượt sử dụng voucher nếu có sử dụng
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank() && voucherResult.okDiscount()) {
            promotionService.incrementVoucherUsage(req.getCouponCode().trim());
        }
        if (req.getShipCode() != null && !req.getShipCode().isBlank() && voucherResult.okShip()) {
            promotionService.incrementVoucherUsage(req.getShipCode().trim());
        }

        return saved;
    }

    /**
     * Gộp 2 mã voucher thành 1 chuỗi để lưu vào voucherCode
     */
    private String buildVoucherCodesString(String couponCode, String shipCode) {
        boolean hasCoupon = couponCode != null && !couponCode.isBlank();
        boolean hasShip = shipCode != null && !shipCode.isBlank();
        
        if (hasCoupon && hasShip) {
            return couponCode.trim() + "," + shipCode.trim();
        } else if (hasCoupon) {
            return couponCode.trim();
        } else if (hasShip) {
            return shipCode.trim();
        } else {
            return null;
        }
    }

    private String genOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ====== Các method còn lại trong interface để compile (tùy bạn implement sau) ======
    @Override
    @Transactional(readOnly = true)
    public List<Order> myOrders(User user) {
        // TODO: implement theo repository của bạn (vd: orderRepo.findByUserOrderByCreatedAtDesc(user))
//        throw new UnsupportedOperationException("Not implemented yet");
        return orderRepo.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> searchForShop(Long shopId, String status, Pageable pageable) {
        // TODO: implement theo repository của bạn
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void changeStatus(String orderCode, String newStatus, Long actorId) {
        // TODO: implement theo repository + lưu history
        throw new UnsupportedOperationException("Not implemented yet");
    }
    @Override
    @Transactional
    public void markPaid(String orderCode){
        var order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        //order.setStatus(OrderStatus.PAID);
        order.setTotal(BigDecimal.ZERO);
        orderRepo.save(order);
        cartService.clearSelected(order.getUser());
    }
}
