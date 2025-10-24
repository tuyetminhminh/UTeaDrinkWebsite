package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.PromotionManagementDTO;
import net.codejava.utea.manager.service.PromotionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý khuyến mãi
     */
    @GetMapping
    public String promotionManagement(
            @AuthenticationPrincipal User currentUser,
            Model model) {
        List<PromotionManagementDTO> promotions = promotionService.getAllPromotions(currentUser.getId());
        model.addAttribute("promotions", promotions);
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long promotionId,
            Model model) {
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
    public ResponseEntity<?> getAllPromotions(@AuthenticationPrincipal User currentUser) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long promotionId) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody PromotionManagementDTO promotionDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long promotionId,
            @RequestBody PromotionManagementDTO promotionDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long promotionId) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long promotionId) {
        try {
            PromotionManagementDTO promotion = promotionService.togglePromotionStatus(currentUser.getId(), promotionId);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

