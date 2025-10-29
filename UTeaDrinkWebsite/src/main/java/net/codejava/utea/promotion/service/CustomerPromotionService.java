package net.codejava.utea.promotion.service;

import net.codejava.utea.promotion.view.PromotionCardVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service cho customer xem danh sách chương trình khuyến mãi tự động
 */
public interface CustomerPromotionService {
    
    /**
     * Lấy danh sách promotions đang active
     * @param shopId ID của shop (null = lấy GLOBAL)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Phân trang
     * @return Page of PromotionCardVM
     */
    Page<PromotionCardVM> getActivePromotions(Long shopId, String keyword, Pageable pageable);
}

