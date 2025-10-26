package net.codejava.utea.manager.entity;

import static jakarta.persistence.FetchType.LAZY;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "shop_inventory", uniqueConstraints = @UniqueConstraint(columnNames = { "shop_id",
		"product_variant_id" }))
public class ShopInventory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	Shop shop;
	
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "product_variant_id", nullable = false)
	net.codejava.utea.catalog.entity.ProductVariant variant;
	
	Integer quantity;
	
	LocalDateTime updatedAt;

	@PrePersist
	@PreUpdate
	void t() {
		updatedAt = LocalDateTime.now();
	}
}
