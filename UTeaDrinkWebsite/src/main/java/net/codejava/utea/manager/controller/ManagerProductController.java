package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.service.ProductManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/manager/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerProductController {

    private final ProductManagementService productService;
    
    /**
     * Helper method to extract User from Authentication
     */
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new IllegalStateException("Không thể xác định người dùng hiện tại");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý sản phẩm
     */
    @GetMapping
    public String productManagement(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        try {
            User currentUser = getCurrentUser(authentication);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductManagementDTO> products = productService.getAllProducts(currentUser.getId(), pageable, null, null, null, null);
            
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("hasShop", true);
            return "manager/product-management";
        } catch (RuntimeException e) {
            model.addAttribute("hasShop", false);
            model.addAttribute("errorMessage", e.getMessage());
            return "manager/product-management";
        }
    }

    /**
     * Trang tạo sản phẩm mới
     */
    @GetMapping("/create")
    public String createProductPage(Model model) {
        return "manager/product-create";
    }

    /**
     * Trang xem chi tiết sản phẩm
     */
    @GetMapping("/{productId}")
    public String productDetailPage(
            Authentication authentication,
            @PathVariable Long productId,
            Model model) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductManagementDTO product = productService.getProductById(currentUser.getId(), productId);
            model.addAttribute("product", product);
            model.addAttribute("hasShop", true);
            return "manager/product-detail";
        } catch (RuntimeException e) {
            model.addAttribute("hasShop", false);
            model.addAttribute("errorMessage", e.getMessage());
            return "manager/product-detail";
        }
    }

    /**
     * Trang quản lý topping
     */
    @GetMapping("/toppings")
    public String toppingManagement(
            @AuthenticationPrincipal User currentUser,
            Model model) {
        List<ToppingDTO> toppings = productService.getAllToppings(currentUser.getId());
        model.addAttribute("toppings", toppings);
        return "manager/topping-management";
    }

    // ==================== API ENDPOINTS - PRODUCT ====================

    /**
     * API: Lấy tất cả sản phẩm (với filter + sort)
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {
        try {
            User currentUser = getCurrentUser(authentication);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductManagementDTO> products = productService.getAllProducts(
                currentUser.getId(), pageable, search, categoryId, status, sortBy);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy chi tiết sản phẩm
     */
    @GetMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<?> getProductById(
            Authentication authentication,
            @PathVariable Long productId) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductManagementDTO product = productService.getProductById(currentUser.getId(), productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Chuyển đổi trạng thái sản phẩm (AVAILABLE <-> HIDDEN)
     */
    @PutMapping("/api/{productId}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleProductStatus(
            Authentication authentication,
            @PathVariable Long productId) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductManagementDTO product = productService.toggleProductStatus(currentUser.getId(), productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - IMAGE ====================

    /**
     * API: Upload ảnh sản phẩm
     */
    @PostMapping("/api/{productId}/images")
    @ResponseBody
    public ResponseEntity<?> uploadImage(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer sortOrder) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductImageDTO image = productService.uploadImage(currentUser.getId(), productId, file, sortOrder);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Thêm ảnh từ URL
     */
    @PostMapping("/api/{productId}/images/url")
    @ResponseBody
    public ResponseEntity<?> addImageFromUrl(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            User currentUser = getCurrentUser(authentication);
            String imageUrl = payload.get("url");
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("URL ảnh không được để trống");
            }
            ProductImageDTO image = productService.addImageFromUrl(currentUser.getId(), productId, imageUrl);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa ảnh sản phẩm
     */
    @DeleteMapping("/api/images/{imageId}")
    @ResponseBody
    public ResponseEntity<?> deleteImage(
            Authentication authentication,
            @PathVariable Long imageId) {
        try {
            User currentUser = getCurrentUser(authentication);
            productService.deleteImage(currentUser.getId(), imageId);
            return ResponseEntity.ok("Ảnh đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - VARIANT ====================

    /**
     * API: Thêm biến thể
     */
    @PostMapping("/api/{productId}/variants")
    @ResponseBody
    public ResponseEntity<?> addVariant(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody ProductVariantDTO variantDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductVariantDTO variant = productService.addVariant(currentUser.getId(), productId, variantDTO);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật biến thể
     */
    @PutMapping("/api/variants/{variantId}")
    @ResponseBody
    public ResponseEntity<?> updateVariant(
            Authentication authentication,
            @PathVariable Long variantId,
            @RequestBody ProductVariantDTO variantDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ProductVariantDTO variant = productService.updateVariant(currentUser.getId(), variantId, variantDTO);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa biến thể
     */
    @DeleteMapping("/api/variants/{variantId}")
    @ResponseBody
    public ResponseEntity<?> deleteVariant(
            Authentication authentication,
            @PathVariable Long variantId) {
        try {
            User currentUser = getCurrentUser(authentication);
            productService.deleteVariant(currentUser.getId(), variantId);
            return ResponseEntity.ok("Biến thể đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - TOPPING ====================

    /**
     * API: Lấy tất cả topping
     */
    @GetMapping("/api/toppings")
    @ResponseBody
    public ResponseEntity<?> getAllToppings(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<ToppingDTO> toppings = productService.getAllToppings(currentUser.getId());
            return ResponseEntity.ok(toppings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo topping mới
     */
    @PostMapping("/api/toppings")
    @ResponseBody
    public ResponseEntity<?> createTopping(
            Authentication authentication,
            @RequestBody ToppingDTO toppingDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ToppingDTO topping = productService.createTopping(currentUser.getId(), toppingDTO);
            return ResponseEntity.ok(topping);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật topping
     */
    @PutMapping("/api/toppings/{toppingId}")
    @ResponseBody
    public ResponseEntity<?> updateTopping(
            Authentication authentication,
            @PathVariable Long toppingId,
            @RequestBody ToppingDTO toppingDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            ToppingDTO topping = productService.updateTopping(currentUser.getId(), toppingId, toppingDTO);
            return ResponseEntity.ok(topping);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa topping
     */
    @DeleteMapping("/api/toppings/{toppingId}")
    @ResponseBody
    public ResponseEntity<?> deleteTopping(
            Authentication authentication,
            @PathVariable Long toppingId) {
        try {
            User currentUser = getCurrentUser(authentication);
            productService.deleteTopping(currentUser.getId(), toppingId);
            return ResponseEntity.ok("Topping đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Toggle topping status (ACTIVE/HIDDEN)
     */
    @PutMapping("/api/toppings/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleToppingStatus(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            User currentUser = getCurrentUser(authentication);
            ToppingDTO topping = productService.toggleToppingStatus(currentUser.getId(), id);
            return ResponseEntity.ok(topping);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - DASHBOARD STATS ====================

    /**
     * API: Đếm tổng số sản phẩm (cho dashboard)
     */
    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<?> getProductCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            System.out.println("=== DEBUG: getProductCount ===");
            System.out.println("Manager ID: " + currentUser.getId());
            
            Pageable pageable = PageRequest.of(0, 1);
            Page<ProductManagementDTO> products = productService.getAllProducts(currentUser.getId(), pageable, null, null, null, null);
            
            long totalProducts = products.getTotalElements();
            System.out.println("Total products: " + totalProducts);
            System.out.println("=== END DEBUG ===");
            
            return ResponseEntity.ok(java.util.Map.of(
                "total", totalProducts,
                "active", totalProducts // TODO: Count only AVAILABLE products
            ));
        } catch (Exception e) {
            System.err.println("ERROR in getProductCount: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy thống kê sản phẩm (cho stats cards)
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getProductStats(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            java.util.Map<String, Object> stats = productService.getProductStats(currentUser.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách categories
     */
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<?> getCategories() {
        try {
            List<java.util.Map<String, Object>> categories = productService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

