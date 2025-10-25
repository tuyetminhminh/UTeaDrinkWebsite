package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.PromotionManagementDTO;
import net.codejava.utea.manager.service.PromotionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerPromotionController {

    private final PromotionManagementService promotionService;

    // ==================== HELPER ====================
    
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Invalid authentication principal");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý khuyến mãi
     */
    @GetMapping
    public String promotionManagement(
            Authentication authentication,
            Model model) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<PromotionManagementDTO> promotions = promotionService.getAllPromotions(currentUser.getId());
            model.addAttribute("promotions", promotions);
            model.addAttribute("hasShop", true);
        } catch (RuntimeException e) {
            // Manager chưa có shop
            model.addAttribute("hasShop", false);
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "manager/promotion-management";
    }

    /**
     * Trang tạo khuyến mãi mới
     */
    @GetMapping("/create")
    public String createPromotionPage() {
        return "manager/promotion-create";
    }

    /**
     * Trang chỉnh sửa khuyến mãi
     */
    @GetMapping("/{promotionId}/edit")
    public String editPromotionPage(
            Authentication authentication,
            @PathVariable Long promotionId,
            Model model) {
        User currentUser = getCurrentUser(authentication);
        PromotionManagementDTO promotion = promotionService.getPromotionById(currentUser.getId(), promotionId);
        model.addAttribute("promotion", promotion);
        return "manager/promotion-edit";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy tất cả khuyến mãi
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllPromotions(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<PromotionManagementDTO> promotions = promotionService.getAllPromotions(currentUser.getId());
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy chi tiết khuyến mãi
     */
    @GetMapping("/api/{promotionId}")
    @ResponseBody
    public ResponseEntity<?> getPromotionById(
            Authentication authentication,
            @PathVariable Long promotionId) {
        try {
            User currentUser = getCurrentUser(authentication);
            PromotionManagementDTO promotion = promotionService.getPromotionById(currentUser.getId(), promotionId);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo khuyến mãi mới
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPromotion(
            Authentication authentication,
            @RequestBody PromotionManagementDTO promotionDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            PromotionManagementDTO createdPromotion = promotionService.createPromotion(currentUser.getId(), promotionDTO);
            return ResponseEntity.ok(createdPromotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật khuyến mãi
     */
    @PutMapping("/api/{promotionId}")
    @ResponseBody
    public ResponseEntity<?> updatePromotion(
            Authentication authentication,
            @PathVariable Long promotionId,
            @RequestBody PromotionManagementDTO promotionDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            PromotionManagementDTO updatedPromotion = promotionService.updatePromotion(currentUser.getId(), promotionId, promotionDTO);
            return ResponseEntity.ok(updatedPromotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa khuyến mãi
     */
    @DeleteMapping("/api/{promotionId}")
    @ResponseBody
    public ResponseEntity<?> deletePromotion(
            Authentication authentication,
            @PathVariable Long promotionId) {
        try {
            User currentUser = getCurrentUser(authentication);
            promotionService.deletePromotion(currentUser.getId(), promotionId);
            return ResponseEntity.ok("Khuyến mãi đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Kích hoạt/Vô hiệu hóa khuyến mãi
     */
    @PutMapping("/api/{promotionId}/toggle")
    @ResponseBody
    public ResponseEntity<?> togglePromotionStatus(
            Authentication authentication,
            @PathVariable Long promotionId) {
        try {
            User currentUser = getCurrentUser(authentication);
            PromotionManagementDTO promotion = promotionService.togglePromotionStatus(currentUser.getId(), promotionId);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

