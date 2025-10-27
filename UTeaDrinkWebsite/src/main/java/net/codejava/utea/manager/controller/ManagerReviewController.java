package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.ReviewDTO;
import net.codejava.utea.manager.dto.ReviewStatsDTO;
import net.codejava.utea.manager.service.ManagerReviewService;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReviewController {

    private final ManagerReviewService reviewService;
    private final ShopService shopService;

    /**
     * Helper: Lấy user hiện tại
     */
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Invalid authentication principal");
    }

    /**
     * Helper: Lấy shopId của manager
     */
    private Long getShopId(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return shopService.getShopByManagerId(currentUser.getId()).getId();
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý đánh giá
     */
    @GetMapping
    public String reviewsPage(Authentication authentication, Model model) {
        try {
            Long shopId = getShopId(authentication);
            model.addAttribute("shopId", shopId);
            return "manager/reviews";
        } catch (Exception e) {
            model.addAttribute("error", "Bạn chưa đăng ký shop. Vui lòng đăng ký shop trước.");
            return "redirect:/manager/shop/register";
        }
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy thống kê đánh giá
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<ReviewStatsDTO> getStats(Authentication authentication) {
        try {
            Long shopId = getShopId(authentication);
            ReviewStatsDTO stats = reviewService.getStats(shopId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy danh sách đánh giá (có phân trang + filter)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<ReviewDTO>> getReviews(
            Authentication authentication,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long shopId = getShopId(authentication);
            Page<ReviewDTO> reviews = reviewService.getReviews(shopId, productId, rating, page, size);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

