package net.codejava.utea.customer.repository;

import net.codejava.utea.customer.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    @EntityGraph(attributePaths = {"size"})
    List<ProductVariant> findByProduct_ProductIdAndStatus(Long productId, String status);
}