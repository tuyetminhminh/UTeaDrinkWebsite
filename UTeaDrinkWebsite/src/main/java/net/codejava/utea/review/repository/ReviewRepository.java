package net.codejava.utea.review.repository;

import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductIdAndStatus(Long productId, ReviewStatus status);
    
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Review> findByStatus(ReviewStatus status);
    
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
}

