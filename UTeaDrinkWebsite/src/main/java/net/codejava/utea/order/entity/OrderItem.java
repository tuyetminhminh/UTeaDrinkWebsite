package net.codejava.utea.order.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = { @Index(name = "ix_oi_order", columnList = "order_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/** biến thể size: S/M/L (có thể null nếu sản phẩm không có variant) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id")
	private ProductVariant variant;

	@Column(nullable = false)
	private Integer quantity;

	/** giá 1 đơn vị (đã bao gồm chênh lệch size) tại thời điểm đặt */
	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal unitPrice;

	/** tổng dòng = unitPrice * quantity + phụ thu topping */
	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal lineTotal;

	/** Lưu topping dưới dạng JSON (đơn giản & ổn cho giai đoạn đầu) */
	@Column(name = "toppings_json", columnDefinition = "NVARCHAR(MAX)")
	private String toppingsJson;

	/** ghi chú của khách */
	@Column(columnDefinition = "NVARCHAR(500)")
	private String note;
}
