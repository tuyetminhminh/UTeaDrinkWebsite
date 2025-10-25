package net.codejava.utea.review.service;

import net.codejava.utea.review.view.ReviewView;
import org.springframework.data.domain.*;

import java.util.Map;

public interface ReviewService {
    Page<ReviewView> listApproved(Long productId, Integer rating, Pageable pageable);
    Double avgRating(Long productId);
    Map<Integer, Long> countByStars(Long productId);
}
