package net.codejava.utea.engagement.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.catalog.entity.Product;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "wishlists",
    uniqueConstraints = @UniqueConstraint(name="uq_wishlist_user_product", columnNames = {"user_id", "product_id"}),
    indexes = @Index(name = "ix_wishlist_user", columnList = "user_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wishlist {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
