package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.service.ProductManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý sản phẩm
     */
    @GetMapping
    public String productManagement(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductManagementDTO> products = productService.getAllProducts(currentUser.getId(), pageable);
        
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        return "manager/product-management";
    }

    /**
     * Trang tạo sản phẩm mới
     */
    @GetMapping("/create")
    public String createProductPage(Model model) {
        return "manager/product-create";
    }

    /**
     * Trang chỉnh sửa sản phẩm
     */
    @GetMapping("/{productId}/edit")
    public String editProductPage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            Model model) {
        ProductManagementDTO product = productService.getProductById(currentUser.getId(), productId);
        model.addAttribute("product", product);
        return "manager/product-edit";
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
     * API: Lấy tất cả sản phẩm
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductManagementDTO> products = productService.getAllProducts(currentUser.getId(), pageable);
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        try {
            ProductManagementDTO product = productService.getProductById(currentUser.getId(), productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo sản phẩm mới
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ProductManagementDTO productDTO) {
        try {
            ProductManagementDTO createdProduct = productService.createProduct(currentUser.getId(), productDTO);
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật sản phẩm
     */
    @PutMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            @RequestBody ProductManagementDTO productDTO) {
        try {
            ProductManagementDTO updatedProduct = productService.updateProduct(currentUser.getId(), productId, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa sản phẩm
     */
    @DeleteMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {
        try {
            productService.deleteProduct(currentUser.getId(), productId);
            return ResponseEntity.ok("Sản phẩm đã được xóa");
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer sortOrder) {
        try {
            ProductImageDTO image = productService.uploadImage(currentUser.getId(), productId, file, sortOrder);
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long imageId) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            @RequestBody ProductVariantDTO variantDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long variantId,
            @RequestBody ProductVariantDTO variantDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long variantId) {
        try {
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
    public ResponseEntity<?> getAllToppings(@AuthenticationPrincipal User currentUser) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestBody ToppingDTO toppingDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long toppingId,
            @RequestBody ToppingDTO toppingDTO) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long toppingId) {
        try {
            productService.deleteTopping(currentUser.getId(), toppingId);
            return ResponseEntity.ok("Topping đã được xóa");
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
    public ResponseEntity<?> getProductCount(@AuthenticationPrincipal User currentUser) {
        try {
            Pageable pageable = PageRequest.of(0, 1);
            Page<ProductManagementDTO> products = productService.getAllProducts(currentUser.getId(), pageable);
            
            return ResponseEntity.ok(java.util.Map.of(
                "total", products.getTotalElements(),
                "active", products.getTotalElements() // TODO: Count only AVAILABLE products
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

