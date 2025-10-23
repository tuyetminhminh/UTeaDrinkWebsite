package net.codejava.utea.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, unique = true, columnDefinition = "NVARCHAR(150)")
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private ProductCategory parent;

	@OneToMany(mappedBy = "parent")
	@Builder.Default
	private List<ProductCategory> children = new ArrayList<>();

	@Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
	private String description;

	@Column(length = 20)
	@Builder.Default
	private String status = "ACTIVE";

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
}
