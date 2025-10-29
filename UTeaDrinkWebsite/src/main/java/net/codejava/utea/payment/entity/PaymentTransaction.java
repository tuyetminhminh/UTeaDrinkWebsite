package net.codejava.utea.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.payment.entity.enums.PaymentMethod;
import net.codejava.utea.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = { @Index(name = "ix_pt_code", columnList = "gatewayTxnCode"),
		@Index(name = "ix_pt_order", columnList = "orderCode") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** lưu để đối soát nhanh mà không cần join Order */
	@Column(length = 40)
	private String orderCode;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal amount;

	/** mã giao dịch phía cổng thanh toán (VNPAY/MoMo) */
	@Column(length = 100)
	private String gatewayTxnCode;

	/** raw payload/response để debug/đối soát */
	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String gatewayPayload;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
