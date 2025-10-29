package net.codejava.utea.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotions", indexes = { @Index(name = "ix_promo_scope", columnList = "scope"),
		@Index(name = "ix_promo_shop", columnList = "shop_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private PromoScope scope = PromoScope.SHOP;

	/** null nếu GLOBAL */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private PromoType type = PromoType.PERCENT;

	/**
	 * luật áp dụng, ví dụ: { "minTotal":100000, "onlyNewUser":false,
	 * "birthdayDaysOffset":7, "categoryIds":[1,2], "productIds":[10,11],
	 * "percentOff":10, "amountCap":30000 }
	 */
	@Column(name = "rule_json", columnDefinition = "NVARCHAR(MAX)")
	private String ruleJson;

	@Column(length = 150, columnDefinition = "NVARCHAR(200)")
	private String title;

	@Column(columnDefinition = "NVARCHAR(500)")
	private String description;

	@Column(name = "active_from")
	private LocalDateTime activeFrom;

	@Column(name = "active_to")
	private LocalDateTime activeTo;

	@Column(length = 20)
	private String status = "ACTIVE"; // ACTIVE/INACTIVE
}
