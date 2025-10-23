package net.codejava.utea.review.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.catalog.entity.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews", indexes = { @Index(name = "ix_review_product", columnList = "product_id"),
		@Index(name = "ix_review_user", columnList = "user_id"),
		@Index(name = "ix_review_status", columnList = "status") }, uniqueConstraints = {
// nếu muốn giới hạn mỗi user chỉ review 1 lần/1 product:
// @UniqueConstraint(name="uq_review_user_product", columnNames = {"user_id",
// "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* Ai đánh giá */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/* Sản phẩm nào */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/* Rating 1..5 */
	@Column(nullable = false)
	private Integer rating;

	/* Nội dung ≥ 50 ký tự (validate ở service/controller) */
	@Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
	private String content;

	/* Gắn tới order để xác minh “đã mua” (tùy logic) */
	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "order_item_id")
	private Long orderItemId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	@Builder.Default
	private ReviewStatus status = ReviewStatus.PENDING;

	@Column(name = "helpful_count")
	@Builder.Default
	private Integer helpfulCount = 0;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/* Media kèm theo */
	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ReviewMedia> mediaList = new ArrayList<>();
}