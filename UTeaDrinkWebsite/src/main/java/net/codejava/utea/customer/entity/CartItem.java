package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", indexes = { @Index(name = "ix_ci_cart", columnList = "cart_id"),
		@Index(name = "ix_ci_product", columnList = "product_id"),
		@Index(name = "ix_ci_variant", columnList = "variant_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY) // mỗi item thuộc 1 cart
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY) // snapshot theo Product/Variant hiện tại
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY) // có thể null nếu SP không có biến thể
	@JoinColumn(name = "variant_id")
	private ProductVariant variant;

	@Column(nullable = false)
	private Integer quantity;

	// chốt giá tại thời điểm thêm vào giỏ (tránh thay đổi giá làm lệch tổng)
	@Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
	private BigDecimal unitPrice;

	// toppings/ghi chú thêm nếu có (JSON tuỳ chọn)
	@Column(name = "toppings_json", columnDefinition = "NVARCHAR(MAX)")
	private String toppingsJson;

	// chọn/bỏ chọn khi checkout (giúp “chọn vài item thanh toán”)
	@Column(nullable = false)
	@Builder.Default
	private boolean selected = true;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
}
