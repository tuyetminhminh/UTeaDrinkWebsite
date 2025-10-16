package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "size_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id", nullable=false)
    private Product product;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="size_id", nullable=false)
    private Size size;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal price; // giá cuối của size

    private Integer stock;

    @Column(length=20)
    private String status = "ACTIVE";
}
