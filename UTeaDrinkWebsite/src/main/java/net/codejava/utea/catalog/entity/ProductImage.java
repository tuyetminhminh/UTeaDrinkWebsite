package net.codejava.utea.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "url", length = 500)
	private String url; // Cloudinary secure URL

	@Column(name = "public_id", length = 200)
	private String publicId; // Cloudinary public_id để xóa/sửa

	@Column(name = "sort_order")
	private Integer sortOrder;
}
