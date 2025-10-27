package net.codejava.utea.engagement.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.catalog.entity.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "viewed_products", uniqueConstraints = @UniqueConstraint(name = "uq_viewed_user_product", columnNames = {
		"user_id", "product_id" }), indexes = @Index(name = "ix_viewed_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewedProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "last_seen_at")
	@Builder.Default
	private LocalDateTime lastSeenAt = LocalDateTime.now();

	public void touch() {
		this.lastSeenAt = LocalDateTime.now();
	}
}
