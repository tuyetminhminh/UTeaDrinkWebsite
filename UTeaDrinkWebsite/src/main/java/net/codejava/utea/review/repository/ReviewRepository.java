package net.codejava.utea.review.repository;

import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.view.ReviewView;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
}
