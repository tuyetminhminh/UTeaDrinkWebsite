package net.codejava.utea.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.manager.entity.Shop;

import java.math.BigDecimal;

@Entity
@Table(name = "toppings", uniqueConstraints = @UniqueConstraint(columnNames = { "shop_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(150)")
	private String name;

	@Column(name = "price", precision = 12, scale = 2, nullable = false)
	private BigDecimal price;

	@Column(length = 20)
	@Builder.Default
	private String status = "ACTIVE";
}
