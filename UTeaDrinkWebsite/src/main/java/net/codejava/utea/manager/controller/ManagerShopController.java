package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.ShopBannerDTO;
import net.codejava.utea.manager.dto.ShopDTO;
import net.codejava.utea.manager.dto.ShopSectionDTO;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý shop
     */
    @GetMapping
    public String shopManagement(@AuthenticationPrincipal User currentUser, Model model) {
        try {
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
    public String registerShopPage() {
        return "manager/shop-register";
    }

    /**
     * Trang quản lý banners
     */
    @GetMapping("/banners")
    public String bannersManagement(@AuthenticationPrincipal User currentUser, Model model) {
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
    public String sectionsManagement(@AuthenticationPrincipal User currentUser, Model model) {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody ShopDTO shopDTO) {
        try {
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
    public ResponseEntity<?> getShopInfo(@AuthenticationPrincipal User currentUser) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody ShopDTO shopDTO) {
        try {
            ShopDTO updatedShop = shopService.updateShop(currentUser.getId(), shopDTO);
            return ResponseEntity.ok(updatedShop);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - BANNERS ====================

    /**
     * API: Lấy tất cả banner
     */
    @GetMapping("/api/banners")
    @ResponseBody
    public ResponseEntity<?> getAllBanners(@AuthenticationPrincipal User currentUser) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody ShopBannerDTO bannerDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long bannerId,
            @RequestBody ShopBannerDTO bannerDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long bannerId) {
        try {
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
    public ResponseEntity<?> getAllSections(@AuthenticationPrincipal User currentUser) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody ShopSectionDTO sectionDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long sectionId,
            @RequestBody ShopSectionDTO sectionDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long sectionId) {
        try {
            shopService.deleteSection(currentUser.getId(), sectionId);
            return ResponseEntity.ok("Section đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
