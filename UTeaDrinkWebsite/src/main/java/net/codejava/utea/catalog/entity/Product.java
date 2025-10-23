package net.codejava.utea.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.manager.entity.Shop; // ⬅ cần entity Shop ở module manager

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = { @Index(name = "ix_product_shop", columnList = "shop_id"),
		@Index(name = "ix_product_cat", columnList = "category_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* Thuộc về shop nào */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	/* Danh mục */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private ProductCategory category;

	@Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(200)")
	private String name;

	@Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
	private String description;

	@Column(name = "base_price", precision = 12, scale = 2, nullable = false)
	private BigDecimal basePrice;

	@Column(name = "sold_count")
	@Builder.Default
	private Integer soldCount = 0;

	@Column(name = "rating_avg", precision = 3, scale = 2)
	private BigDecimal ratingAvg; // 0.00 - 5.00

	@Column(length = 20)
	@Builder.Default
	private String status = "AVAILABLE"; // AVAILABLE | HIDDEN | OUT_OF_STOCK

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/* Ảnh và biến thể */
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ProductImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ProductVariant> variants = new ArrayList<>();

}
