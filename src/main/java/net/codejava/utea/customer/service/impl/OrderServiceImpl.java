package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.customer.entity.Order;
import net.codejava.utea.customer.entity.OrderItem;
import net.codejava.utea.entity.*;
import net.codejava.utea.customer.entity.enums.OrderStatus;
import net.codejava.utea.customer.repository.CartRepository;
import net.codejava.utea.customer.repository.CouponRepository;
import net.codejava.utea.customer.repository.OrderRepository;
import net.codejava.utea.customer.service.CouponService;
import net.codejava.utea.customer.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final CouponRepository couponRepo;
    private final CouponService couponService;

    @Override
    @Transactional
    public Order createFromCart(Customer customer,
                                UnaryOperator<Order.OrderBuilder> orderConfigurer,
                                CheckoutRequest req) {

        Objects.requireNonNull(customer, "customer is required");

        List<Cart> selected = cartRepo.findByCustomerAndIsSelectedTrue(customer);
        if (selected.isEmpty()) throw new IllegalStateException("Giỏ hàng trống (chưa chọn sản phẩm).");

        BigDecimal subtotal = selected.stream()
                .map(c -> c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = estimateShippingFee(subtotal);

        var apply = couponService.apply(req.getCouponCode(), subtotal, shippingFee);
        BigDecimal discount = apply.ok() ? apply.discount() : BigDecimal.ZERO;
        BigDecimal total    = subtotal.add(shippingFee).subtract(discount);

        Order.OrderBuilder builder = Order.builder()
                .orderCode(genOrderCode())
                .customer(customer)
                .status(OrderStatus.PENDING.name())
                .paymentMethod(req.getPaymentMethod())
                .subtotalAmount(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .shippingDiscount(BigDecimal.ZERO)
                .totalAmount(total);

        if (orderConfigurer != null) builder = orderConfigurer.apply(builder);
        Order order = builder.build();

        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            couponRepo.findByCodeActiveNow(req.getCouponCode().trim())
                    .ifPresent(order::setAppliedOrderCoupon);
        }

        List<OrderItem> items = selected.stream().map(ci -> OrderItem.builder()
                .order(order)
                .product(ci.getProduct())
                .productName(ci.getProduct().getName())
                .imageUrl(ci.getProduct().getImageUrl())
                .quantity(ci.getQuantity())
                .unitPrice(ci.getUnitPrice())
                .lineTotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .build()
        ).collect(Collectors.toList());
        order.setItems(items);

        Order saved = orderRepo.save(order);
        cartRepo.deleteSelectedByCustomer(customer);
        return saved;
    }

    private String genOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal estimateShippingFee(BigDecimal subtotal) {
        // Rule demo: >= 500k miễn phí, ngược lại 15k
        return (subtotal.compareTo(new BigDecimal("500000")) >= 0)
                ? BigDecimal.ZERO
                : new BigDecimal("15000");
    }
}
