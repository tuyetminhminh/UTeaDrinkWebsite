//package net.codejava.utea.review.repository;
//
//import net.codejava.utea.review.entity.Review;
//import net.codejava.utea.review.entity.enums.ReviewStatus;
//import net.codejava.utea.review.view.ReviewView;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.repository.*;
//import org.springframework.data.repository.query.Param;
//import net.codejava.utea.review.dto.ReviewCardDTO;
//
//import java.util.List;
//
//public interface ReviewRepository extends JpaRepository<Review, Long> {
//
//    // (giữ nguyên các query avg/count nếu bạn đang dùng)
//    @Query("select avg(r.rating) from Review r where r.product.id=:pid and r.status = :st")
//    Double avgRating(@Param("pid") Long productId, @Param("st") ReviewStatus status);
//
//    /**
//     * Tính average rating cho nhiều products cùng lúc (batch query)
//     */
//    @Query("""
//        select r.product.id, avg(r.rating)
//        from Review r
//        where r.product.id in :productIds
//        and r.status = :status
//        group by r.product.id
//    """)
//    java.util.List<Object[]> avgRatingByProducts(@Param("productIds") java.util.Collection<Long> productIds,
//                                                  @Param("status") ReviewStatus status);
//
//    @Query("select r.rating as star, count(r) as cnt from Review r where r.product.id=:pid and r.status=:st group by r.rating")
//    java.util.List<Object[]> countByStarsRaw(@Param("pid") Long productId, @Param("st") ReviewStatus status);
//
//    // NEW: lấy thẳng ReviewView, join user để tránh lazy
//    @Query(
//            value = """
//            select new net.codejava.utea.review.view.ReviewView(
//                r.id,
//                coalesce(u.fullName, 'Khách'),
//                r.rating,
//                r.content,
//                r.createdAt
//            )
//            from Review r
//            join r.user u
//            where r.product.id = :pid
//              and r.status = :st
//              and (:rating is null or r.rating = :rating)
//            order by r.createdAt desc
//        """,
//            countQuery = """
//            select count(r)
//            from Review r
//            where r.product.id = :pid
//              and r.status = :st
//              and (:rating is null or r.rating = :rating)
//        """
//    )
//    Page<ReviewView> findApprovedView(@Param("pid") Long productId,
//                                      @Param("rating") Integer rating,
//                                      @Param("st") ReviewStatus status,
//                                      Pageable pageable);
//
//    // ==================== MANAGER QUERIES ====================
//
//    /**
//     * Lấy tất cả đánh giá của các sản phẩm thuộc shop
//     */
//    @Query("""
//        SELECT r FROM Review r
//        JOIN FETCH r.product p
//        JOIN FETCH r.user u
//        WHERE p.shop.id = :shopId
//        ORDER BY r.createdAt DESC
//    """)
//    Page<Review> findByShopId(@Param("shopId") Long shopId, Pageable pageable);
//
//    /**
//     * Lọc đánh giá theo sản phẩm và rating
//     */
//    @Query("""
//        SELECT r FROM Review r
//        JOIN FETCH r.product p
//        JOIN FETCH r.user u
//        WHERE p.shop.id = :shopId
//        AND (:productId IS NULL OR p.id = :productId)
//        AND (:rating IS NULL OR r.rating = :rating)
//        ORDER BY r.createdAt DESC
//    """)
//    Page<Review> findByShopIdFiltered(@Param("shopId") Long shopId,
//                                       @Param("productId") Long productId,
//                                       @Param("rating") Integer rating,
//                                       Pageable pageable);
//
//    /**
//     * Đếm tổng số đánh giá của shop
//     */
//    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.shop.id = :shopId")
//    Long countByShopId(@Param("shopId") Long shopId);
//
//    /**
//     * Tính rating trung bình của shop
//     */
//    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.shop.id = :shopId")
//    Double avgRatingByShopId(@Param("shopId") Long shopId);
//
//    /**
//     * Đếm số lượng đánh giá theo từng rating của shop
//     */
//    @Query("""
//        SELECT r.rating, COUNT(r)
//        FROM Review r
//        WHERE r.product.shop.id = :shopId
//        GROUP BY r.rating
//    """)
//    java.util.List<Object[]> countByRatingForShop(@Param("shopId") Long shopId);
//
//    @Query("""
//    select new net.codejava.utea.review.dto.ReviewCardDTO(
//        coalesce(p.name, 'Sản phẩm'),
//        r.content
//    )
//    from Review r
//    left join r.product p
//    where r.status = :st and r.rating = 5
//    order by r.createdAt desc
//""")
//    List<ReviewCardDTO> findTopCardsForHome(@Param("st") ReviewStatus status, Pageable pageable);
//}

package net.codejava.utea.review.repository;

import net.codejava.utea.review.dto.ReviewCardDTO;
import net.codejava.utea.review.dto.ReviewModerationRow;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.view.ReviewView;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // (giữ nguyên các query avg/count nếu bạn đang dùng)
    @Query("select avg(r.rating) from Review r where r.product.id=:pid and r.status = :st")
    Double avgRating(@Param("pid") Long productId, @Param("st") ReviewStatus status);

    @Query("select r.rating as star, count(r) as cnt from Review r where r.product.id=:pid and r.status=:st group by r.rating")
    java.util.List<Object[]> countByStarsRaw(@Param("pid") Long productId, @Param("st") ReviewStatus status);

    // NEW: lấy thẳng ReviewView, join user để tránh lazy
    @Query(
            value = """
            select new net.codejava.utea.review.view.ReviewView(
                r.id,
                coalesce(u.fullName, 'Khách'),
                r.rating,
                r.content,
                r.createdAt
            )
            from Review r
            join r.user u
            where r.product.id = :pid
              and r.status = :st
              and (:rating is null or r.rating = :rating)
            order by r.createdAt desc
        """,
            countQuery = """
            select count(r)
            from Review r
            where r.product.id = :pid
              and r.status = :st
              and (:rating is null or r.rating = :rating)
        """
    )
    Page<ReviewView> findApprovedView(@Param("pid") Long productId,
                                      @Param("rating") Integer rating,
                                      @Param("st") ReviewStatus status,
                                      Pageable pageable);

    // ==================== MANAGER QUERIES ====================

    /**
     * Lấy tất cả đánh giá của các sản phẩm thuộc shop
     */
    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.product p
        JOIN FETCH r.user u
        WHERE p.shop.id = :shopId
        ORDER BY r.createdAt DESC
    """)
    Page<Review> findByShopId(@Param("shopId") Long shopId, Pageable pageable);

    /**
     * Lọc đánh giá theo sản phẩm và rating
     */
    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.product p
        JOIN FETCH r.user u
        WHERE p.shop.id = :shopId
        AND (:productId IS NULL OR p.id = :productId)
        AND (:rating IS NULL OR r.rating = :rating)
        ORDER BY r.createdAt DESC
    """)
    Page<Review> findByShopIdFiltered(@Param("shopId") Long shopId,
                                      @Param("productId") Long productId,
                                      @Param("rating") Integer rating,
                                      Pageable pageable);

    /**
     * Đếm tổng số đánh giá của shop
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.shop.id = :shopId")
    Long countByShopId(@Param("shopId") Long shopId);

    /**
     * Tính rating trung bình của shop
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.shop.id = :shopId")
    Double avgRatingByShopId(@Param("shopId") Long shopId);

    /**
     * Đếm số lượng đánh giá theo từng rating của shop
     */
    @Query("""
        SELECT r.rating, COUNT(r) 
        FROM Review r 
        WHERE r.product.shop.id = :shopId 
        GROUP BY r.rating
    """)
    java.util.List<Object[]> countByRatingForShop(@Param("shopId") Long shopId);

    // ==================== ADMIN QUERIES ====================
    @Query(value = """
            SELECT new net.codejava.utea.review.dto.ReviewModerationRow(
                r.id, p.name, u.fullName, r.rating, r.content, r.status, r.createdAt
            )
            FROM Review r JOIN r.product p JOIN r.user u
            WHERE (:status IS NULL OR r.status = :status)
              AND (:kw IS NULL OR :kw = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')))
            """,
            countQuery = """
            SELECT COUNT(r) FROM Review r JOIN r.product p JOIN r.user u
            WHERE (:status IS NULL OR r.status = :status)
              AND (:kw IS NULL OR :kw = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')))
            """)
    Page<ReviewModerationRow> searchForModeration(@Param("status") ReviewStatus status,
                                                  @Param("kw") String kw,
                                                  Pageable pageable);
    @Query("""
        select r.product.id as productId, avg(r.rating) as avgRating
        from Review r
        where r.product.id in :ids
          and r.status = :st
        group by r.product.id
    """)
    java.util.List<Object[]> avgRatingByProducts(@Param("ids") Collection<Long> productIds,
                                                 @Param("st") ReviewStatus status);
    @Query("""
    select new net.codejava.utea.review.dto.ReviewCardDTO(
        coalesce(p.name, 'Sản phẩm'),
        r.content
    )
    from Review r
    left join r.product p
    where r.status = :st and r.rating = 5
    order by r.createdAt desc
""")
    List<ReviewCardDTO> findTopCardsForHome(@Param("st") ReviewStatus status, Pageable pageable);
}
