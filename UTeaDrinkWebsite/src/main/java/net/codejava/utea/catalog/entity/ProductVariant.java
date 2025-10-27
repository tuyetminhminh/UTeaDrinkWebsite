package net.codejava.utea.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.catalog.entity.enums.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id", "size" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Enumerated(EnumType.STRING)
	@Column(name = "size", nullable = false, length = 10)
	private Size size; // S / M / L / XL

	@Column(name = "price", precision = 12, scale = 2, nullable = false)
	private BigDecimal price;

	/* Tùy chọn: nếu muốn hiển thị dung tích */
	@Column(name = "volume_ml")
	private Integer volumeMl; // ví dụ: 350, 500...
}
