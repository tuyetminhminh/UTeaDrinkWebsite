package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.customer.entity.enums.PaymentMethod;
import net.codejava.utea.customer.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name="method", length=20, nullable=false)
    private PaymentMethod method;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="status", length=20, nullable=false)
    private PaymentStatus status = PaymentStatus.INIT;

    @Column(name="amount", precision=12, scale=2, nullable=false)
    private BigDecimal amount;

    @Column(name="provider_txn_id", length=100)
    private String providerTxnId;

    @Column(name="paid_at")
    private LocalDateTime paidAt;

    @Builder.Default
    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.INIT;
    }
}