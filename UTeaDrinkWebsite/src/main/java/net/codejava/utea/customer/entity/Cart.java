package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = @Index(name = "ix_cart_user", columnList = "user_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CartItem> items = new ArrayList<>();

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	@PreUpdate
	void touch() {
		this.updatedAt = LocalDateTime.now();
	}
}
