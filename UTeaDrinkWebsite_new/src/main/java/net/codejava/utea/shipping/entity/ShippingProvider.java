package net.codejava.utea.shipping.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	/** phí cơ bản (km đầu hoặc đơn vị chuẩn) */
	@Column(precision = 10, scale = 2, nullable = false)
	private BigDecimal baseFee = BigDecimal.ZERO;

	/** quy tắc vùng/phụ phí, lưu JSON: {distanceSteps:[…],regionExtra:[…]} */
	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String regionRulesJson;

	@Column(length = 20)
	private String status = "ACTIVE";

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}
