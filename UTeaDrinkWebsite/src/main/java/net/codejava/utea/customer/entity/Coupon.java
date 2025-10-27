package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.customer.entity.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "coupons", indexes = {
        @Index(name="IX_coupons_code", columnList = "code", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name="code", nullable=false, length=50, unique=true)
    private String code;

    @Column(name="title", nullable=false, length=200, columnDefinition = "nvarchar(200)")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name="type", length=20, nullable=false)
    private CouponType type;

    @Column(name="discount_value", precision=12, scale=2, nullable=false)
    private BigDecimal discountValue;

    @Column(name="min_order_value", precision=12, scale=2)
    private BigDecimal minOrderValue;

    @Column(name="max_discount", precision=12, scale=2)
    private BigDecimal maxDiscount;

    @Column(name="usage_limit")
    private Integer usageLimit;

    @Builder.Default
    @Column(name="used_count")
    private Integer usedCount = 0;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="end_date")
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name="active")
    private Boolean active = true;

    @Builder.Default
    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null) active = true;
        if (usedCount == null) usedCount = 0;
    }
}
