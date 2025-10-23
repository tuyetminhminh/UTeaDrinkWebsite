package net.codejava.utea.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.base.Auditable;
import net.codejava.utea.common.entity.User;

@Entity
@Table(name = "shop_managers", uniqueConstraints = @UniqueConstraint(columnNames = { "shop_id", "account_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopManager extends Auditable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private User manager; // account.role = "MANAGER"
}
