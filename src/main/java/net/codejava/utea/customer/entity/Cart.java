package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;  // null khi sp không có size

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Builder.Default
    @Column(name = "is_selected", nullable = false)
    private boolean isSelected = false;

    @Builder.Default
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    @Version
    private Long version;

    @Transient
    public BigDecimal getLineTotal() {
        return unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    }
}
