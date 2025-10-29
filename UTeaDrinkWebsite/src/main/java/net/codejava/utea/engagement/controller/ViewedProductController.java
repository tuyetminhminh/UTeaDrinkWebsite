package net.codejava.utea.engagement.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.engagement.service.ViewedProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/recently-viewed")
@RequiredArgsConstructor
public class ViewedProductController {

    private final ViewedProductService viewedProductService;

    /**
     * Lấy danh sách sản phẩm đã xem gần đây
     * @param limit số lượng sản phẩm (default 10)
     */
    @GetMapping
    public ResponseEntity<?> getRecentlyViewed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Kiểm tra đăng nhập
        if (userDetails == null || userDetails.getUser() == null) {
            response.put("ok", false);
            response.put("reason", "NOT_LOGGED_IN");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userDetails.getUser().getId();
            List<Product> products = viewedProductService.getRecentlyViewedProducts(userId, limit);
            
            // Convert sang DTO để trả về JSON
            List<Map<String, Object>> productDtos = products.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            
            response.put("ok", true);
            response.put("products", productDtos);
            response.put("total", productDtos.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("ok", false);
            response.put("reason", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Xóa lịch sử xem
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null || userDetails.getUser() == null) {
            response.put("ok", false);
            response.put("reason", "NOT_LOGGED_IN");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userDetails.getUser().getId();
            viewedProductService.clearHistory(userId);
            
            response.put("ok", true);
            response.put("message", "Đã xóa lịch sử xem");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("ok", false);
            response.put("reason", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Convert Product entity sang DTO
     */
    private Map<String, Object> toDto(Product product) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", product.getId());
        dto.put("name", product.getName());
        dto.put("basePrice", product.getBasePrice());
        dto.put("ratingAvg", product.getRatingAvg());
        dto.put("soldCount", product.getSoldCount());
        dto.put("status", product.getStatus());
        
        // Image URL
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getUrl();
        }
        dto.put("imageUrl", imageUrl);
        
        // Category
        if (product.getCategory() != null) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", product.getCategory().getId());
            category.put("name", product.getCategory().getName());
            dto.put("category", category);
        }
        
        return dto;
    }
}

