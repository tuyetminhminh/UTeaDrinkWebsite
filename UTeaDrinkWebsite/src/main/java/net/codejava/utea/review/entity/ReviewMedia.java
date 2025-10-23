package net.codejava.utea.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMedia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", nullable = false)
	private Review review;

	@Column(name = "media_type", length = 20)
	private String mediaType; // IMAGE | VIDEO (tuỳ bạn định nghĩa)

	@Column(name = "url", length = 500)
	private String url; // Cloudinary secure URL

	@Column(name = "public_id", length = 200)
	private String publicId; // Cloudinary public_id (xóa/sửa)
}
