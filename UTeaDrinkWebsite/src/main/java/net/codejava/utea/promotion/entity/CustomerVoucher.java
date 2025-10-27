package net.codejava.utea.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_vouchers",
        uniqueConstraints = @UniqueConstraint(name="uk_cv_user_voucher", columnNames = {"user_id","voucher_id"}),
        indexes = {
                @Index(name="ix_cv_user", columnList = "user_id"),
                @Index(name="ix_cv_voucher", columnList = "voucher_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerVoucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="voucher_id", nullable=false)
    private net.codejava.utea.promotion.entity.Voucher voucher;

    @Column(name="saved_at", nullable=false)
    private LocalDateTime savedAt;

    /** ACTIVE/REMOVED – dùng REMOVED khi bỏ lưu (hoặc bạn có thể delete record) */
    @Column(length=12, nullable=false)
    private String state = "ACTIVE";
}