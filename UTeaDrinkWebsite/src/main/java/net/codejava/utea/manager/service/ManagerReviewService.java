package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.ReviewDTO;
import net.codejava.utea.manager.dto.ReviewStatsDTO;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagerReviewService {

    private final ReviewRepository reviewRepo;

    /**
     * Lấy danh sách đánh giá của shop (có phân trang)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviews(Long shopId, Long productId, Integer rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepo.findByShopIdFiltered(shopId, productId, rating, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Lấy thống kê đánh giá của shop
     */
    @Transactional(readOnly = true)
    public ReviewStatsDTO getStats(Long shopId) {
        Long total = reviewRepo.countByShopId(shopId);
        Double avgRating = reviewRepo.avgRatingByShopId(shopId);
        
        if (avgRating == null) {
            avgRating = 0.0;
        }
        
        // Đếm số lượng đánh giá theo từng rating
        List<Object[]> ratingCounts = reviewRepo.countByRatingForShop(shopId);
        Map<Integer, Long> countMap = new HashMap<>();
        for (Object[] row : ratingCounts) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            countMap.put(rating, count);
        }
        
        return ReviewStatsDTO.builder()
                .totalReviews(total)
                .averageRating(java.math.BigDecimal.valueOf(avgRating))
                .ratingCounts(countMap)
                .build();
    }

    /**
     * Convert Review entity sang DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .productImage(review.getProduct().getMainImageUrl())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userEmail(review.getUser().getEmail())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .helpfulCount(review.getHelpfulCount())
                .build();
    }
}

