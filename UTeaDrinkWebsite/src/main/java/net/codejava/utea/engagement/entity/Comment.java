package net.codejava.utea.engagement.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.catalog.entity.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments",
       indexes = {
           @Index(name = "ix_comment_product", columnList = "product_id"),
           @Index(name = "ix_comment_user", columnList = "user_id")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_approved") @Builder.Default
    private boolean approved = true; // nếu muốn duyệt bình luận
}
