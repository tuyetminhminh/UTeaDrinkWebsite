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
import net.codejava.utea.promotion.service.PromotionResult;
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

        // 3) apply voucher/promo
        PromotionResult pr = (req.getCouponCode() == null || req.getCouponCode().isBlank())
                ? promotionService.empty(subtotal, shippingFee)
                : promotionService.applyVoucher(req.getCouponCode().trim(), subtotal, shippingFee);

        BigDecimal discount = pr.discount();
        BigDecimal total    = pr.total(); // subtotal + shipping - discount

        // 4) build Order theo entity thực tế
        // LƯU Ý: Order bắt buộc có shop -> giả định giỏ hàng 1 shop; lấy từ item đầu tiên
        var firstShop = selected.get(0).getProduct().getShop();

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
                .voucherCode(req.getCouponCode());

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
        selected.forEach(i -> cartService.removeItem(user, i.getId()));
        return saved;
    }

    private String genOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ====== Các method còn lại trong interface để compile (tùy bạn implement sau) ======
    @Override
    @Transactional(readOnly = true)
    public List<Order> myOrders(User user) {
        // TODO: implement theo repository của bạn (vd: orderRepo.findByUserOrderByCreatedAtDesc(user))
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void markPaid(String orderCode){
        var order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.PAID);
        orderRepo.save(order);
    }
}
