/*
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
import net.codejava.utea.payment.entity.PaymentTransaction;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
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

    private final CartService cartService;               // Giỏ hàng theo User
    private final OrderRepository orderRepo;
    private final PromotionService promotionService;     // Áp dụng voucher/promotion
    private final PaymentTransactionRepository paymentTxnRepo;

    // ====== Tạo đơn từ giỏ hàng ======
    @Override
    public Order createFromCart(User user,
                                Consumer<Order.OrderBuilder> extra,
                                CheckoutRequest req) {

        // 1) Lấy item đã chọn
        List<CartItem> selected = cartService.listSelected(user);
        if (selected == null || selected.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống (chưa chọn sản phẩm).");
        }

        // 2) Tính subtotal + ship
        BigDecimal subtotal = selected.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = cartService.estimateShippingFee(subtotal);

        // 3a) Áp dụng voucher (mã nhập) – xử lý cả couponCode & shipCode
        var voucherResult = promotionService.applyBoth(
                req.getCouponCode(), req.getShipCode(), subtotal, shippingFee, user);

        // 3b) Khuyến mãi tự động
        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, user);

        // 3c) Ưu tiên: Voucher > Promotion (không cộng dồn)
        BigDecimal productDiscount = voucherResult.okDiscount()
                ? voucherResult.discount()
                : autoPromos.discount().discount();

        BigDecimal freeshipDiscount = voucherResult.okShip()
                ? voucherResult.shipDiscount()
                : autoPromos.freeship().discount();

        BigDecimal discount = productDiscount.add(freeshipDiscount);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        // 4) Build Order (giả định giỏ 1 shop)
        var firstShop = selected.get(0).getProduct().getShop();
        String voucherCodes = buildVoucherCodesString(req.getCouponCode(), req.getShipCode());

        Order.OrderBuilder ob = Order.builder()
                .orderCode(genOrderCode())
                .user(user)
                .shop(firstShop)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .voucherCode(voucherCodes);

        if (extra != null) extra.accept(ob);
        Order order = ob.build();

        // 5) Order items
        List<OrderItem> items = new ArrayList<>(selected.size());
        for (CartItem ci : selected) {
            items.add(OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .variant(ci.getVariant()) // có thể null
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .lineTotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build());
        }
        order.setItems(items);

        // 6) Lưu đơn
        Order saved = orderRepo.save(order);

        // 7) Cập nhật lượt sử dụng voucher nếu có
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank() && voucherResult.okDiscount()) {
            promotionService.incrementVoucherUsage(req.getCouponCode().trim());
        }
        if (req.getShipCode() != null && !req.getShipCode().isBlank() && voucherResult.okShip()) {
            promotionService.incrementVoucherUsage(req.getShipCode().trim());
        }

        return saved;
    }

    */
/** Gộp 2 mã voucher thành 1 chuỗi để lưu vào voucherCode *//*

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

    // ====== Truy vấn đơn của tôi ======
    @Override
    @Transactional(readOnly = true)
    public List<Order> myOrders(User user) {
        return orderRepo.findByUserOrderByCreatedAtDesc(user);
    }

    // ====== Tìm kiếm cho shop (tuỳ bạn implement sau) ======
    @Override
    @Transactional(readOnly = true)
    public Page<Order> searchForShop(Long shopId, String status, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ====== Đổi trạng thái thủ công (tuỳ bạn implement sau) ======
    @Override
    public void changeStatus(String orderCode, String newStatus, Long actorId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ====== Đánh dấu đã thanh toán (tương thích ngược, KHÔNG đụng total) ======
    @Override
    @Transactional
    public void markPaid(String orderCode) {
        markPaid(orderCode, null, null);
    }

    // ====== Đánh dấu đã thanh toán + (tuỳ chọn) đối soát số tiền & gắn PaymentTransaction ======
    @Override
    @Transactional
    public void markPaid(String orderCode, String paymentTxnIdStr, BigDecimal paidAmount) {
        var order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderCode));

        // Idempotent
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        // (Tuỳ chọn) đối soát số tiền
        if (paidAmount != null && order.getTotal() != null
                && order.getTotal().compareTo(paidAmount) != 0) {
            throw new IllegalStateException("Paid amount mismatched: order="
                    + order.getTotal() + ", paid=" + paidAmount);
        }

        // (Tuỳ chọn) gắn PaymentTransaction vào đơn, nếu có id
        if (paymentTxnIdStr != null && !paymentTxnIdStr.isBlank()) {
            try {
                Long paymentTxnId = Long.valueOf(paymentTxnIdStr);
                PaymentTransaction txn = paymentTxnRepo.findById(paymentTxnId)
                        .orElse(null);
                if (txn != null) {
                    order.setPayment(txn); // <— đúng với entity của bạn
                }
            } catch (NumberFormatException ignore) {
                // bỏ qua nếu không phải số
            }
        }

        // ✅ Chỉ cập nhật trạng thái; KHÔNG bao giờ set total = 0
        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

        // Dọn item đã chọn trong giỏ sau khi thanh toán thành công
        cartService.clearSelected(order.getUser());
    }
}
*/



package net.codejava.utea.order.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.service.AddressService;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
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

    private final CartService cartService;
    private final OrderRepository orderRepo;
    private final PromotionService promotionService;
    private final PaymentTransactionRepository paymentTxnRepo;
    private final AddressService addressService;   // ✅ THÊM

    @Override
    public Order createFromCart(User user,
                                Consumer<Order.OrderBuilder> extra,
                                CheckoutRequest req) {

        // 1) Lấy item đã chọn
        List<CartItem> selected = cartService.listSelected(user);
        if (selected == null || selected.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống (chưa chọn sản phẩm).");
        }

        // 2) Tính subtotal + ship
        BigDecimal subtotal = selected.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = cartService.estimateShippingFee(subtotal);

        // 3) Ưu đãi (voucher trước, promotion sau)
        var voucherResult = promotionService.applyBoth(
                req.getCouponCode(), req.getShipCode(), subtotal, shippingFee, user);

        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, user);

        BigDecimal productDiscount = voucherResult.okDiscount()
                ? voucherResult.discount()
                : autoPromos.discount().discount();

        BigDecimal freeshipDiscount = voucherResult.okShip()
                ? voucherResult.shipDiscount()
                : autoPromos.freeship().discount();

        BigDecimal discount = productDiscount.add(freeshipDiscount);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        // 4) Lấy/gắn địa chỉ giao hàng
        Address pickedAddr = null;
        if (req.getAddressId() != null) {
            pickedAddr = addressService.findById(req.getAddressId())
                    .filter(a -> a.getUser() != null && a.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new IllegalStateException("Địa chỉ không hợp lệ."));
        } else {
            // Nếu không chọn từ sổ địa chỉ mà nhập tay -> tạo Address mới (lưu luôn cho user)
            boolean hasTypedAddress =
                    nonBlank(req.getFullname()) ||
                            nonBlank(req.getPhone()) ||
                            nonBlank(req.getAddressLine()) ||
                            nonBlank(req.getDistrict()) ||
                            nonBlank(req.getProvince());

            if (hasTypedAddress) {
                Address a = new Address();
                a.setUser(user);
                a.setReceiverName(nullToEmpty(req.getFullname()));
                a.setPhone(nullToEmpty(req.getPhone()));
                a.setLine(nullToEmpty(req.getAddressLine()));
                a.setWard(nullToEmpty(req.getDistrict()));   // nếu bạn có field ward riêng, sửa lại cho đúng
                a.setDistrict(nullToEmpty(req.getDistrict()));
                a.setProvince(nullToEmpty(req.getProvince()));
                a.setDefault(false);
                pickedAddr = addressService.save(a);
            }
        }

        // 5) Build Order (giả định 1 shop)
        var firstShop = selected.get(0).getProduct().getShop();
        String voucherCodes = buildVoucherCodesString(req.getCouponCode(), req.getShipCode());

        Order.OrderBuilder ob = Order.builder()
                .orderCode(genOrderCode())
                .user(user)
                .shop(firstShop)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .voucherCode(voucherCodes)
                .shippingAddress(pickedAddr);   // ✅ ĐÚNG TÊN FIELD TRONG ENTITY

        if (extra != null) extra.accept(ob);
        Order order = ob.build();

        // 6) Order items
        List<OrderItem> items = new ArrayList<>(selected.size());
        for (CartItem ci : selected) {
            items.add(OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .variant(ci.getVariant())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .lineTotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build());
        }
        order.setItems(items);

        // 7) Lưu đơn
        Order saved = orderRepo.save(order);

        // 8) Tăng lượt dùng voucher (nếu hợp lệ)
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank() && voucherResult.okDiscount()) {
            promotionService.incrementVoucherUsage(req.getCouponCode().trim());
        }
        if (req.getShipCode() != null && !req.getShipCode().isBlank() && voucherResult.okShip()) {
            promotionService.incrementVoucherUsage(req.getShipCode().trim());
        }

        return saved;
    }

    private boolean nonBlank(String s){ return s != null && !s.isBlank(); }
    private String nullToEmpty(String s){ return s == null ? "" : s; }

    private String buildVoucherCodesString(String couponCode, String shipCode) {
        boolean hasCoupon = couponCode != null && !couponCode.isBlank();
        boolean hasShip = shipCode != null && !shipCode.isBlank();
        if (hasCoupon && hasShip) return couponCode.trim() + "," + shipCode.trim();
        if (hasCoupon) return couponCode.trim();
        if (hasShip) return shipCode.trim();
        return null;
    }

    private String genOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> myOrders(User user) { return orderRepo.findByUserOrderByCreatedAtDesc(user); }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> searchForShop(Long shopId, String status, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void changeStatus(String orderCode, String newStatus, Long actorId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public void markPaid(String orderCode) { markPaid(orderCode, null, null); }

    @Override
    @Transactional
    public void markPaid(String orderCode, String paymentTxnIdStr, BigDecimal paidAmount) {
        var order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderCode));

        if (order.getStatus() == OrderStatus.PAID) return;

        if (paidAmount != null && order.getTotal() != null
                && order.getTotal().compareTo(paidAmount) != 0) {
            throw new IllegalStateException("Paid amount mismatched: order=" + order.getTotal() + ", paid=" + paidAmount);
        }

        if (paymentTxnIdStr != null && !paymentTxnIdStr.isBlank()) {
            try {
                Long paymentTxnId = Long.valueOf(paymentTxnIdStr);
                paymentTxnRepo.findById(paymentTxnId).ifPresent(order::setPayment);
            } catch (NumberFormatException ignore) {}
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

        cartService.clearSelected(order.getUser());
    }
}
