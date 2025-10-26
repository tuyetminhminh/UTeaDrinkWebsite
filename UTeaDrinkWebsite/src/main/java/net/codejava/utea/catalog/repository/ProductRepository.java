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

    @EntityGraph(attributePaths = { "shop", "category", "images" })
    Optional<Product> findWithAllById(Long id);

    // JPQL search: dùng basePrice (KHÔNG phải price), category.id, name
    @Query("""
			    SELECT p FROM Product p
			    WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
			      AND (:cat IS NULL OR p.category.id = :cat)
			      AND (:min IS NULL OR p.basePrice >= :min)
			      AND (:max IS NULL OR p.basePrice <= :max)
			      AND p.status = 'AVAILABLE'
			""")
    Page<Product> search(@Param("kw") String kw, @Param("cat") Long categoryId, @Param("min") BigDecimal min,
                         @Param("max") BigDecimal max, Pageable pageable);

    // JPQL adminSearch: dùng status, shop.id, name
    @EntityGraph(attributePaths = { "shop", "images" }) // <<< quan trọng
    @Query("""
			    SELECT p FROM Product p
			    WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
			      AND (:shopId IS NULL OR p.shop.id = :shopId)
			      AND (:status = 'ALL' OR p.status = :status)
			    ORDER BY p.id DESC
			""")
    Page<Product> adminSearch(@Param("q") String q, @Param("shopId") Long shopId, @Param("status") String status,
                              Pageable pageable);

    // ==================== SECTION QUERIES ====================
    
    /**
     * Sản phẩm nổi bật: rating cao nhất (tính từ reviews APPROVED)
     * Sắp xếp theo rating trung bình từ cao đến thấp
     */
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Review r ON r.product.id = p.id AND r.status = 'APPROVED'
        WHERE p.shop.id = :shopId 
          AND p.status = 'AVAILABLE'
        GROUP BY p
        ORDER BY COALESCE(AVG(r.rating), 0) DESC, COUNT(r.id) DESC, p.createdAt DESC
    """)
    List<Product> findFeaturedProducts(@Param("shopId") Long shopId, Pageable pageable);
    
    /**
     * Sản phẩm mới: theo thời gian tạo
     */
    @EntityGraph(attributePaths = {"images", "category"})
    @Query("""
        SELECT p FROM Product p
        WHERE p.shop.id = :shopId 
          AND p.status = 'AVAILABLE'
        ORDER BY p.createdAt DESC
    """)
    List<Product> findNewArrivals(@Param("shopId") Long shopId, Pageable pageable);
    
    /**
     * Sản phẩm bán chạy: 
     * - Ưu tiên 1: Số lượng đã bán từ orders DELIVERED (cao → thấp)
     * - Ưu tiên 2: Tổng số lượng từ tất cả orders (cao → thấp)
     * - Ưu tiên 3: Sản phẩm mới nhất
     */
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN OrderItem oi ON oi.product.id = p.id
        LEFT JOIN oi.order o
        WHERE p.shop.id = :shopId 
          AND p.status = 'AVAILABLE'
        GROUP BY p
        ORDER BY 
          COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN oi.quantity ELSE 0 END), 0) DESC,
          COALESCE(SUM(oi.quantity), 0) DESC,
          p.createdAt DESC
    """)
    List<Product> findTopSelling(@Param("shopId") Long shopId, Pageable pageable);
    
    /**
     * Sản phẩm khuyến mãi: lấy sản phẩm có rating tốt và bán chạy
     * - Lọc: rating >= 4.0
     * - Ưu tiên 1: Số lượng đã bán từ orders DELIVERED (cao → thấp)
     * - Ưu tiên 2: Tổng số lượng từ tất cả orders (cao → thấp)
     * - Ưu tiên 3: Rating cao hơn
     */
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Review r ON r.product.id = p.id AND r.status = 'APPROVED'
        LEFT JOIN OrderItem oi ON oi.product.id = p.id
        LEFT JOIN oi.order o
        WHERE p.shop.id = :shopId 
          AND p.status = 'AVAILABLE'
        GROUP BY p
        HAVING COALESCE(AVG(r.rating), 0) >= 4.0
        ORDER BY 
          COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN oi.quantity ELSE 0 END), 0) DESC,
          COALESCE(SUM(oi.quantity), 0) DESC,
          COALESCE(AVG(r.rating), 0) DESC
    """)
    List<Product> findPromotionProducts(@Param("shopId") Long shopId, Pageable pageable);

}
