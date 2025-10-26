package net.codejava.utea.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.catalog.entity.Product;

@Entity
@Table(name = "promotion_products", indexes = { @Index(name = "ix_pp_promo", columnList = "promotion_id"),
		@Index(name = "ix_pp_product", columnList = "product_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id", nullable = false)
	private Promotion promotion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
}
