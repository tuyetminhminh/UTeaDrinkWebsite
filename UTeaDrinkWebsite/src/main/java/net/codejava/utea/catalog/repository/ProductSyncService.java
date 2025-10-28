package net.codejava.utea.catalog.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service để đồng bộ sold_count và rating_avg vào database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;

    /**
     * Đồng bộ sold_count và rating_avg cho TẤT CẢ sản phẩm
     */
    @Transactional
    public void syncAllProducts() {
        log.info("🔄 Starting sync for ALL products...");
        long startTime = System.currentTimeMillis();

        List<Product> allProducts = productRepo.findAll();
        int updated = 0;

        for (Product product : allProducts) {
            boolean changed = syncProduct(product);
            if (changed) {
                updated++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Sync completed! Updated {}/{} products in {}ms", 
                updated, allProducts.size(), duration);
    }

    /**
     * Đồng bộ sold_count và rating_avg cho 1 sản phẩm
     */
    @Transactional
    public boolean syncProduct(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) {
            log.warn("Product {} not found", productId);
            return false;
        }
        return syncProduct(product);
    }

    /**
     * Sync một product entity
     */
    private boolean syncProduct(Product product) {
        boolean changed = false;

        // 1. Tính sold_count từ OrderItems DELIVERED
        int actualSoldCount = calculateSoldCount(product.getId());
        if (product.getSoldCount() == null || product.getSoldCount() != actualSoldCount) {
            product.setSoldCount(actualSoldCount);
            changed = true;
            log.debug("Product {}: soldCount {} → {}", 
                    product.getId(), product.getSoldCount(), actualSoldCount);
        }

        // 2. Tính rating_avg từ Reviews APPROVED
        BigDecimal actualRating = calculateRatingAvg(product.getId());
        if (actualRating != null) {
            if (product.getRatingAvg() == null || 
                product.getRatingAvg().compareTo(actualRating) != 0) {
                product.setRatingAvg(actualRating);
                changed = true;
                log.debug("Product {}: ratingAvg {} → {}", 
                        product.getId(), product.getRatingAvg(), actualRating);
            }
        } else {
            // Không có review nào → set null hoặc 0
            if (product.getRatingAvg() != null) {
                product.setRatingAvg(null);
                changed = true;
                log.debug("Product {}: ratingAvg cleared (no reviews)", product.getId());
            }
        }

        if (changed) {
            productRepo.save(product);
        }

        return changed;
    }

    /**
     * Tính sold_count từ OrderItems của các đơn DELIVERED
     */
    private int calculateSoldCount(Long productId) {
        List<Order> deliveredOrders = orderRepo.findAll().stream()
                .filter(order -> OrderStatus.DELIVERED.equals(order.getStatus()))
                .toList();

        int totalSold = 0;
        for (Order order : deliveredOrders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct() != null && 
                        item.getProduct().getId().equals(productId)) {
                        totalSold += item.getQuantity();
                    }
                }
            }
        }

        return totalSold;
    }

    /**
     * Tính rating_avg từ Reviews APPROVED
     */
    private BigDecimal calculateRatingAvg(Long productId) {
        Double avgRating = reviewRepo.avgRating(productId, ReviewStatus.APPROVED);
        
        if (avgRating == null || avgRating == 0.0) {
            return null;
        }

        // Round to 2 decimal places
        return BigDecimal.valueOf(avgRating)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Batch sync - hiệu quả hơn cho nhiều sản phẩm
     */
    @Transactional
    public Map<String, Object> syncAllProductsBatch() {
        log.info("🔄 Starting BATCH sync for ALL products...");
        long startTime = System.currentTimeMillis();

        List<Product> allProducts = productRepo.findAll();
        
        // Build maps một lần cho tất cả products
        Map<Long, Integer> soldCountMap = buildSoldCountMap();
        Map<Long, BigDecimal> ratingMap = buildRatingMap();

        int updated = 0;
        for (Product product : allProducts) {
            boolean changed = false;

            // Update sold_count
            Integer newSoldCount = soldCountMap.getOrDefault(product.getId(), 0);
            if (product.getSoldCount() == null || !product.getSoldCount().equals(newSoldCount)) {
                product.setSoldCount(newSoldCount);
                changed = true;
            }

            // Update rating_avg
            BigDecimal newRating = ratingMap.get(product.getId());
            if (newRating != null) {
                if (product.getRatingAvg() == null || product.getRatingAvg().compareTo(newRating) != 0) {
                    product.setRatingAvg(newRating);
                    changed = true;
                }
            } else {
                if (product.getRatingAvg() != null) {
                    product.setRatingAvg(null);
                    changed = true;
                }
            }

            if (changed) {
                productRepo.save(product);
                updated++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ BATCH sync completed! Updated {}/{} products in {}ms", 
                updated, allProducts.size(), duration);

        return Map.of(
            "total", allProducts.size(),
            "updated", updated,
            "duration", duration
        );
    }

    /**
     * Build map sold_count cho tất cả products (1 query)
     */
    private Map<Long, Integer> buildSoldCountMap() {
        Map<Long, Integer> map = new HashMap<>();
        
        List<Order> deliveredOrders = orderRepo.findAll().stream()
                .filter(order -> OrderStatus.DELIVERED.equals(order.getStatus()))
                .toList();

        for (Order order : deliveredOrders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct() != null) {
                        Long productId = item.getProduct().getId();
                        map.put(productId, map.getOrDefault(productId, 0) + item.getQuantity());
                    }
                }
            }
        }

        return map;
    }

    /**
     * Build map rating_avg cho tất cả products (batch query)
     */
    private Map<Long, BigDecimal> buildRatingMap() {
        Map<Long, BigDecimal> map = new HashMap<>();
        
        // Lấy tất cả product IDs
        List<Long> productIds = productRepo.findAll().stream()
                .map(Product::getId)
                .toList();

        // Batch query
        List<Object[]> results = reviewRepo.avgRatingByProducts(
                productIds.stream().collect(java.util.stream.Collectors.toSet()), 
                ReviewStatus.APPROVED
        );

        for (Object[] row : results) {
            Long productId = (Long) row[0];
            Double avgRating = (Double) row[1];
            if (avgRating != null && avgRating > 0) {
                map.put(productId, BigDecimal.valueOf(avgRating)
                        .setScale(2, RoundingMode.HALF_UP));
            }
        }

        return map;
    }
}

