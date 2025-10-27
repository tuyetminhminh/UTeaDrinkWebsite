package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_Id(Long productId);

    @Query("select coalesce(max(pi.sortOrder),0) from ProductImage pi where pi.product.id = :productId")
    Optional<Integer> findMaxSortOrderByProductId(@Param("productId") Long productId);

    Optional<ProductImage> findFirstByProductIdOrderBySortOrderAsc(Long productId);
}