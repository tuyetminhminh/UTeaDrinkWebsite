/*
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

    // ========== Trang sản phẩm chung ==========
    Page<Product> findByCategory_IdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    List<Product> findTop6ByStatusOrderBySoldCountDesc(String status);

    List<Product> findByCategory_NameAndStatusOrderByCreatedAtDesc(String name, String status);

    Optional<Product> findByIdAndStatus(Long id, String status);

    Optional<Product> findByNameAndShopId(String name, Long shopId);

    @EntityGraph(attributePaths = {"shop", "category", "images"})
    Optional<Product> findWithAllById(Long id);

    @Query("""
        SELECT p FROM Product p
        WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:cat IS NULL OR p.category.id = :cat)
          AND (:min IS NULL OR p.basePrice >= :min)
          AND (:max IS NULL OR p.basePrice <= :max)
          AND p.status = 'AVAILABLE'
    """)
    Page<Product> search(@Param("kw") String kw,
                         @Param("cat") Long categoryId,
                         @Param("min") BigDecimal min,
                         @Param("max") BigDecimal max,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "images"})
    @Query("""
        SELECT p FROM Product p
        WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:shopId IS NULL OR p.shop.id = :shopId)
          AND (:status = 'ALL' OR p.status = :status)
        ORDER BY p.id DESC
    """)
    Page<Product> adminSearch(@Param("q") String q,
                              @Param("shopId") Long shopId,
                              @Param("status") String status,
                              Pageable pageable);

    // ========== Queries phục vụ Home/Sections (Bước 3) ==========

    */
/**
     * Lấy danh sách ID sản phẩm nổi bật theo shop (theo rating trung bình cao → thấp)
     *//*

    @Query("""
        select p.id from Product p
        where p.shop.id = :shopId
          and p.status = 'AVAILABLE'
          and p.ratingAvg is not null
        order by p.ratingAvg desc, p.soldCount desc, p.id desc
    """)
    List<Long> findFeaturedIds(@Param("shopId") Long shopId, Pageable pageable);

    */
/**
     * Lấy danh sách ID sản phẩm bán chạy theo shop (dựa trên cột soldCount)
     * Thứ tự sẽ được giữ lại ở Service bằng orderByIds(...)
     *//*

    @Query("""
        select p.id from Product p
        where p.shop.id = :shopId
          and p.status = 'AVAILABLE'
        order by p.soldCount desc, p.id desc
    """)
    List<Long> findTopSellingIds(@Param("shopId") Long shopId, Pageable pageable);

    */
/**
     * Lấy danh sách ID sản phẩm mới nhất theo shop
     *//*

    @Query("""
        select p.id from Product p
        where p.shop.id = :shopId
          and p.status = 'AVAILABLE'
        order by p.createdAt desc, p.id desc
    """)
    List<Long> findNewestIds(@Param("shopId") Long shopId, Pageable pageable);

    */
/**
     * Lấy đầy đủ entity + images (và tránh N+1) dựa theo danh sách ID đã chọn.
     * Việc sắp xếp theo thứ tự IDs thực hiện ở Service (orderByIds).
     *//*

    @Query("""
        select distinct p from Product p
        left join fetch p.images
        left join fetch p.category
        left join fetch p.shop
        where p.id in :ids
          and p.status = 'AVAILABLE'
    """)
    List<Product> findByIdsWithImages(@Param("ids") List<Long> ids);

    // (Nếu muốn thêm phiên bản lọc theo shop, có thể mở comment dưới)
    // @Query("""
    //     select distinct p from Product p
    //     left join fetch p.images
    //     left join fetch p.category
    //     left join fetch p.shop
    //     where p.id in :ids and p.shop.id = :shopId and p.status = 'AVAILABLE'
    // """)
    // List<Product> findByIdsWithImagesAndShop(@Param("shopId") Long shopId, @Param("ids") List<Long> ids);
}
*/
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

    // ========== Trang sản phẩm chung ==========
    Page<Product> findByCategory_IdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    List<Product> findTop6ByStatusOrderBySoldCountDesc(String status);

    List<Product> findByCategory_NameAndStatusOrderByCreatedAtDesc(String name, String status);

    Optional<Product> findByIdAndStatus(Long id, String status);

    Optional<Product> findByNameAndShopId(String name, Long shopId);

    @EntityGraph(attributePaths = {"shop", "category", "images"})
    Optional<Product> findWithAllById(Long id);

    @Query("""
        SELECT p FROM Product p
        WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:cat IS NULL OR p.category.id = :cat)
          AND (:min IS NULL OR p.basePrice >= :min)
          AND (:max IS NULL OR p.basePrice <= :max)
          AND p.status = 'AVAILABLE'
    """)
    Page<Product> search(@Param("kw") String kw,
                         @Param("cat") Long categoryId,
                         @Param("min") BigDecimal min,
                         @Param("max") BigDecimal max,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "images"})
    @Query("""
        SELECT p FROM Product p
        WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:shopId IS NULL OR p.shop.id = :shopId)
          AND (:status = 'ALL' OR p.status = :status)
        ORDER BY p.id DESC
    """)
    Page<Product> adminSearch(@Param("q") String q,
                              @Param("shopId") Long shopId,
                              @Param("status") String status,
                              Pageable pageable);

    // ========== Queries phục vụ Home/Sections (Bước 3) ==========

    /**
     * Lấy danh sách ID sản phẩm bán chạy theo shop (dựa trên cột soldCount hiện có để LIMIT)
     * Thứ tự sẽ được giữ lại ở Service bằng orderByIds(...)
     */
    @Query("""
        select p.id from Product p
        where p.shop.id = :shopId
          and p.status = 'AVAILABLE'
        order by p.soldCount desc, p.id desc
    """)
    List<Long> findTopSellingIds(@Param("shopId") Long shopId, Pageable pageable);

    /**
     * Lấy danh sách ID sản phẩm mới nhất theo shop
     */
    @Query("""
        select p.id from Product p
        where p.shop.id = :shopId
          and p.status = 'AVAILABLE'
        order by p.createdAt desc, p.id desc
    """)
    List<Long> findNewestIds(@Param("shopId") Long shopId, Pageable pageable);

    /**
     * Lấy đầy đủ entity + images (và tránh N+1) dựa theo danh sách ID đã chọn.
     * Việc sắp xếp theo thứ tự IDs thực hiện ở Service (orderByIds).
     */
    @Query("""
        select distinct p from Product p
        left join fetch p.images
        left join fetch p.category
        left join fetch p.shop
        where p.id in :ids
          and p.status = 'AVAILABLE'
    """)
    List<Product> findByIdsWithImages(@Param("ids") List<Long> ids);

    // (Nếu muốn thêm phiên bản lọc theo shop, có thể mở comment dưới)
    @Query("""
        select distinct p from Product p
        left join fetch p.images
        left join fetch p.category
        left join fetch p.shop
        where p.id in :ids and p.shop.id = :shopId and p.status = 'AVAILABLE'
    """)
    List<Product> findByIdsWithImagesAndShop(@Param("shopId") Long shopId, @Param("ids") List<Long> ids);

    boolean existsByShop_Id(Long shopId);
    long countByStatus(String status);
}
