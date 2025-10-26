package net.codejava.utea.review.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.review.service.ReviewService;
import net.codejava.utea.review.view.ReviewView;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;

    @Override
    public Page<ReviewView> listApproved(Long productId, Integer rating, Pageable pageable) {
        // GỌN: trả về projection luôn, không đụng tới entity lazy
        return repo.findApprovedView(productId, rating, ReviewStatus.APPROVED, pageable);
    }

    @Override
    public Double avgRating(Long productId) {
        return repo.avgRating(productId, ReviewStatus.APPROVED);
    }

    @Override
    public Map<Integer, Long> countByStars(Long productId) {
        List<Object[]> rows = repo.countByStarsRaw(productId, ReviewStatus.APPROVED);
        Map<Integer, Long> m = new HashMap<>();
        for (Object[] r : rows) {
            Integer star = ((Number) r[0]).intValue();
            Long cnt = ((Number) r[1]).longValue();
            m.put(star, cnt);
        }
        for (int s = 1; s <= 5; s++) m.putIfAbsent(s, 0L);
        return m;
    }
}
