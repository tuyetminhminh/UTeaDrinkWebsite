package net.codejava.utea.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.catalog.repository.ProductSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller để sync sold_count và rating_avg
 * CHỈ ADMIN mới được phép gọi
 */
@RestController
@RequestMapping("/api/admin/products/sync")
@RequiredArgsConstructor
@Slf4j
public class ProductSyncController {

    private final ProductSyncService syncService;

    /**
     * Sync TẤT CẢ sản phẩm (batch mode - nhanh hơn)
     * GET /api/admin/products/sync/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncAllProducts() {
        log.info("🔄 Admin requested sync all products");
        
        try {
            Map<String, Object> result = syncService.syncAllProductsBatch();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đồng bộ thành công",
                "data", result
            ));
            
        } catch (Exception e) {
            log.error("❌ Error syncing products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Sync 1 sản phẩm cụ thể
     * GET /api/admin/products/sync/{productId}
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncProduct(@PathVariable Long productId) {
        log.info("🔄 Admin requested sync product {}", productId);
        
        try {
            boolean updated = syncService.syncProduct(productId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", updated ? "Đã cập nhật" : "Không có thay đổi",
                "productId", productId,
                "updated", updated
            ));
            
        } catch (Exception e) {
            log.error("❌ Error syncing product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint đơn giản không cần auth (để test)
     * NHỚ XÓA SAU KHI PRODUCTION!
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSync() {
        log.warn("⚠️ TEST SYNC CALLED - Should be removed in production!");
        
        try {
            Map<String, Object> result = syncService.syncAllProductsBatch();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test sync completed",
                "data", result
            ));
            
        } catch (Exception e) {
            log.error("❌ Error in test sync: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
}

