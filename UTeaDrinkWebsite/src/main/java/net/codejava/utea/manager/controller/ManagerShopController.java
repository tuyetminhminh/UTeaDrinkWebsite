package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.ShopBannerDTO;
import net.codejava.utea.manager.dto.ShopDTO;
import net.codejava.utea.manager.dto.ShopSectionDTO;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/shop")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerShopController {

    private final ShopService shopService;
    private final net.codejava.utea.manager.repository.ShopManagerRepository shopManagerRepo;

    // ==================== HELPER METHODS ====================
    
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Invalid authentication principal");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý shop
     */
    @GetMapping
    public String shopManagement(Authentication authentication, Model model) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
            model.addAttribute("shop", shop);
            model.addAttribute("hasShop", true);
        } catch (Exception e) {
            model.addAttribute("hasShop", false);
        }
        return "manager/shop-management";
    }

    /**
     * Trang đăng ký shop (hiển thị form)
     */
    @GetMapping("/register")
    public String registerShopPage(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        // Kiểm tra xem manager đã có shop chưa
        if (shopManagerRepo.existsByManager_Id(currentUser.getId())) {
            // Nếu đã có shop, chuyển về trang quản lý shop
            return "redirect:/manager/shop";
        }
        return "manager/shop-register";
    }

    /**
     * Trang quản lý banners
     */
    @GetMapping("/banners")
    public String bannersManagement(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
        List<ShopBannerDTO> banners = shopService.getAllBanners(shop.getId());
        
        model.addAttribute("shop", shop);
        model.addAttribute("banners", banners);
        return "manager/shop-banners";
    }

    /**
     * Trang quản lý sections
     */
    @GetMapping("/sections")
    public String sectionsManagement(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
        List<ShopSectionDTO> sections = shopService.getAllSections(shop.getId());
        
        model.addAttribute("shop", shop);
        model.addAttribute("sections", sections);
        return "manager/shop-sections";
    }

    // ==================== API ENDPOINTS - SHOP ====================

    /**
     * API: Đăng ký shop mới
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerShop(
            Authentication authentication,
            @RequestBody ShopDTO shopDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO createdShop = shopService.createShop(currentUser.getId(), shopDTO);
            return ResponseEntity.ok(createdShop);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy thông tin shop
     */
    @GetMapping("/api/info")
    @ResponseBody
    public ResponseEntity<?> getShopInfo(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
            return ResponseEntity.ok(shop);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật thông tin shop
     */
    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateShop(
            Authentication authentication,
            @RequestBody ShopDTO shopDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO updatedShop = shopService.updateShop(currentUser.getId(), shopDTO);
            return ResponseEntity.ok(updatedShop);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== PUBLIC API - GET ACTIVE BANNERS ====================
    
    /**
     * API công khai để khách hàng xem banner của shop
     */
    @GetMapping("/api/public/shops/{shopId}/banners")
    @ResponseBody
    public ResponseEntity<List<ShopBannerDTO>> getPublicBanners(@PathVariable Long shopId) {
        try {
            List<ShopBannerDTO> activeBanners = shopService.getActiveBanners(shopId);
            return ResponseEntity.ok(activeBanners);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list if error
        }
    }

    // ==================== API ENDPOINTS - BANNERS (MANAGER) ====================

    /**
     * API: Lấy tất cả banner
     */
    @GetMapping("/api/banners")
    @ResponseBody
    public ResponseEntity<?> getAllBanners(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
            List<ShopBannerDTO> banners = shopService.getAllBanners(shop.getId());
            return ResponseEntity.ok(banners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo banner mới
     */
    @PostMapping("/api/banners")
    @ResponseBody
    public ResponseEntity<?> createBanner(
            Authentication authentication,
            @RequestBody ShopBannerDTO bannerDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopBannerDTO createdBanner = shopService.createBanner(currentUser.getId(), bannerDTO);
            return ResponseEntity.ok(createdBanner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật banner
     */
    @PutMapping("/api/banners/{bannerId}")
    @ResponseBody
    public ResponseEntity<?> updateBanner(
            Authentication authentication,
            @PathVariable Long bannerId,
            @RequestBody ShopBannerDTO bannerDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopBannerDTO updatedBanner = shopService.updateBanner(currentUser.getId(), bannerId, bannerDTO);
            return ResponseEntity.ok(updatedBanner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa banner
     */
    @DeleteMapping("/api/banners/{bannerId}")
    @ResponseBody
    public ResponseEntity<?> deleteBanner(
            Authentication authentication,
            @PathVariable Long bannerId) {
        try {
            User currentUser = getCurrentUser(authentication);
            shopService.deleteBanner(currentUser.getId(), bannerId);
            return ResponseEntity.ok("Banner đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - SECTIONS ====================

    /**
     * API: Lấy tất cả section
     */
    @GetMapping("/api/sections")
    @ResponseBody
    public ResponseEntity<?> getAllSections(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopDTO shop = shopService.getShopByManagerId(currentUser.getId());
            List<ShopSectionDTO> sections = shopService.getAllSections(shop.getId());
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo section mới
     */
    @PostMapping("/api/sections")
    @ResponseBody
    public ResponseEntity<?> createSection(
            Authentication authentication,
            @RequestBody ShopSectionDTO sectionDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopSectionDTO createdSection = shopService.createSection(currentUser.getId(), sectionDTO);
            return ResponseEntity.ok(createdSection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật section
     */
    @PutMapping("/api/sections/{sectionId}")
    @ResponseBody
    public ResponseEntity<?> updateSection(
            Authentication authentication,
            @PathVariable Long sectionId,
            @RequestBody ShopSectionDTO sectionDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShopSectionDTO updatedSection = shopService.updateSection(currentUser.getId(), sectionId, sectionDTO);
            return ResponseEntity.ok(updatedSection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa section
     */
    @DeleteMapping("/api/sections/{sectionId}")
    @ResponseBody
    public ResponseEntity<?> deleteSection(
            Authentication authentication,
            @PathVariable Long sectionId) {
        try {
            User currentUser = getCurrentUser(authentication);
            shopService.deleteSection(currentUser.getId(), sectionId);
            return ResponseEntity.ok("Section đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
