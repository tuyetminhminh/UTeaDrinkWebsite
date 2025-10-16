package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.customer.entity.enums.OrderStatus;
import net.codejava.utea.customer.entity.enums.PaymentMethod;
import net.codejava.utea.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code", length = 30, nullable = false, unique = true)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "shipper_id")
    private Long shipperId;

    // bạn đang để String cho status
    @Builder.Default
    @Column(length = 20, nullable = false)
    private String status = OrderStatus.NEW.name();

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "subtotal_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotalAmount;

    @Builder.Default
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "shipping_discount_percent", precision = 5, scale = 2)
    private BigDecimal shippingDiscountPercent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "shipping_discount", precision = 12, scale = 2)
    private BigDecimal shippingDiscount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "applied_order_coupon_id")
    private Coupon appliedOrderCoupon;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "applied_ship_coupon_id")
    private Coupon appliedShipCoupon;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.NEW.name();
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;
        if (shippingDiscountPercent == null) shippingDiscountPercent = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (shippingDiscount == null) shippingDiscount = BigDecimal.ZERO;
    }
}

