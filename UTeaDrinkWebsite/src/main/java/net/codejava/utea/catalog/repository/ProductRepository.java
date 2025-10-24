package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Đổi categoryId -> id; status giữ nguyên
    Page<Product> findByCategory_IdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    List<Product> findTop6ByStatusOrderBySoldCountDesc(String status);

    // Đổi categoryName -> name
    List<Product> findByCategory_NameAndStatusOrderByCreatedAtDesc(String name, String status);

    Optional<Product> findByIdAndStatus(Long id, String status);

    Optional<Product> findByNameAndShopId(String name, Long shopId);

    // JPQL search: dùng basePrice (KHÔNG phải price), category.id, name
    @Query("""
        SELECT p FROM Product p
        WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:cat IS NULL OR p.category.id = :cat)
          AND (:min IS NULL OR p.basePrice >= :min)
          AND (:max IS NULL OR p.basePrice <= :max)
          AND p.status = 'AVAILABLE'
    """)
    Page<Product> search(
            @Param("kw") String kw,
            @Param("cat") Long categoryId,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            Pageable pageable
    );
}
