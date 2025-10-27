package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.customer.dto.ProductCardDTO;
import net.codejava.utea.customer.dto.ProductImageDTO;
import net.codejava.utea.customer.dto.SectionDTO;
import net.codejava.utea.customer.service.PublicShopService;
import net.codejava.utea.manager.service.ShopService;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderItemRepository;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicShopServiceImpl implements PublicShopService {

    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;
    private final ReviewRepository reviewRepo;
    private final ShopService shopService;

    private static final int LIMIT = 8;

    @Override
    public List<SectionDTO> buildSections(Long shopId) {
        try {
            // ✅ Load sections từ database (do manager tạo)
            List<java.util.Map<String, Object>> dbSections = shopService.getActiveSectionsWithProducts(shopId);
            
            if (dbSections != null && !dbSections.isEmpty()) {
                // Convert từ Map sang SectionDTO
                Map<Long, Integer> soldMap = computeSoldCountMap(shopId);
                Map<Long, BigDecimal> ratingMap = computeRatingMap(shopId);
                
                return dbSections.stream().map(sectionMap -> {
                    String title = (String) sectionMap.get("title");
                    String sectionType = (String) sectionMap.get("sectionType");
                    
                    @SuppressWarnings("unchecked")
                    List<Product> products = (List<Product>) sectionMap.get("products");
                    
                    List<ProductCardDTO> productCards = products != null 
                        ? products.stream().map(p -> toCard(p, soldMap, ratingMap)).toList()
                        : List.of();
                    
                    return new SectionDTO(sectionType, title, productCards);
                }).toList();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error loading sections from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        // ⚠️ Fallback: Nếu không có sections trong DB, tạo mặc định
        return buildDefaultSections(shopId);
    }
    
    /**
     * Tạo sections mặc định nếu manager chưa tạo
     */
    private List<SectionDTO> buildDefaultSections(Long shopId) {
        Map<Long, Integer> soldMap = computeSoldCountMap(shopId);
        Map<Long, BigDecimal> ratingMap = computeRatingMap(shopId);
        List<SectionDTO> sections = new ArrayList<>();

        // ===== FEATURED theo rating THỰC TẾ từ reviews (cao → thấp) =====
        List<Long> featuredIds = getFeaturedProductIds(shopId, LIMIT, ratingMap);
        if (!featuredIds.isEmpty()) {
            var featuredProducts = orderByIds(productRepo.findByIdsWithImages(featuredIds), featuredIds);
            sections.add(new SectionDTO(
                    "FEATURED",
                    "Nổi bật",
                    featuredProducts.stream().map(p -> toCard(p, soldMap, ratingMap)).toList()
            ));
        }

        // ===== TOP_SELLING theo số bán thực tế từ orders =====
        List<Long> topSellingIds = getTopSellingProductIds(shopId, LIMIT);
        if (!topSellingIds.isEmpty()) {
            var topProducts = orderByIds(productRepo.findByIdsWithImages(topSellingIds), topSellingIds);
            sections.add(new SectionDTO(
                    "TOP_SELLING",
                    "Bán chạy",
                    topProducts.stream().map(p -> toCard(p, soldMap, ratingMap)).toList()
            ));
        }

        // ===== NEW_ARRIVALS (sp mới) =====
        var newIds = productRepo.findNewestIds(shopId, PageRequest.of(0, LIMIT));
        if (!newIds.isEmpty()) {
            var newProducts = orderByIds(productRepo.findByIdsWithImages(newIds), newIds);
            sections.add(new SectionDTO(
                    "NEW_ARRIVALS",
                    "Mới ra mắt",
                    newProducts.stream().map(p -> toCard(p, soldMap, ratingMap)).toList()
            ));
        }

        return sections;
    }
    
    /**
     * Lấy danh sách product IDs nổi bật - SẮP XẾP THEO RATING, lấy đủ limit
     */
    private List<Long> getFeaturedProductIds(Long shopId, int limit, Map<Long, BigDecimal> ratingMap) {
        List<Product> allProducts = productRepo.findAll().stream()
                .filter(p -> p.getShop() != null && p.getShop().getId().equals(shopId))
                .filter(p -> "AVAILABLE".equals(p.getStatus()))
                .toList();
        
        if (allProducts.isEmpty()) {
            return List.of();
        }
        
        // Bước 1: Lấy products CÓ RATING (sắp xếp cao → thấp)
        List<Long> featuredIds = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        
        allProducts.stream()
                .filter(p -> ratingMap.containsKey(p.getId()))
                .sorted((p1, p2) -> {
                    BigDecimal rating1 = ratingMap.get(p1.getId());
                    BigDecimal rating2 = ratingMap.get(p2.getId());
                    int ratingCompare = rating2.compareTo(rating1);
                    if (ratingCompare != 0) return ratingCompare;
                    return Integer.compare(
                        p2.getSoldCount() != null ? p2.getSoldCount() : 0,
                        p1.getSoldCount() != null ? p1.getSoldCount() : 0
                    );
                })
                .limit(limit)
                .forEach(p -> {
                    featuredIds.add(p.getId());
                    seenIds.add(p.getId());
                });
        
        System.out.println("⭐ Featured Step 1: " + featuredIds.size() + " products with reviews");
        
        // Bước 2: Nếu chưa đủ, lấy thêm products CHƯA CÓ RATING (sản phẩm mới nhất)
        if (featuredIds.size() < limit) {
            allProducts.stream()
                    .filter(p -> !seenIds.contains(p.getId())) // Chưa có rating
                    .sorted((p1, p2) -> {
                        if (p1.getCreatedAt() == null) return 1;
                        if (p2.getCreatedAt() == null) return -1;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    })
                    .limit(limit - featuredIds.size())
                    .forEach(p -> featuredIds.add(p.getId()));
            
            System.out.println("⭐ Featured Step 2: Total " + featuredIds.size() + " products (added products without reviews)");
        }
        
        return featuredIds;
    }
    
    /**
     * Lấy danh sách product IDs bán chạy nhất - ƯU TIÊN DELIVERED, lấy đủ limit
     */
    private List<Long> getTopSellingProductIds(Long shopId, int limit) {
        List<Long> topIds = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        
        // Bước 1: Lấy từ đơn DELIVERED (đã giao thực sự)
        EnumSet<OrderStatus> deliveredStatuses = EnumSet.of(OrderStatus.DELIVERED);
        var deliveredRows = orderItemRepo.topBestSellersByShop(shopId, deliveredStatuses, PageRequest.of(0, limit * 2));
        
        for (var row : deliveredRows) {
            if (row.getProduct() != null && topIds.size() < limit) {
                Long productId = row.getProduct().getId();
                if (seenIds.add(productId)) {
                    topIds.add(productId);
                }
            }
        }
        
        System.out.println("🔥 Step 1 - From DELIVERED: " + topIds.size() + " products");
        
        // Bước 2: Nếu chưa đủ, lấy thêm từ các trạng thái khác (DELIVERING, PAID, CONFIRMED)
        if (topIds.size() < limit) {
            EnumSet<OrderStatus> otherStatuses = EnumSet.of(OrderStatus.DELIVERING, OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.PREPARING);
            var otherRows = orderItemRepo.topBestSellersByShop(shopId, otherStatuses, PageRequest.of(0, limit * 3));
            
            for (var row : otherRows) {
                if (row.getProduct() != null && topIds.size() < limit) {
                    Long productId = row.getProduct().getId();
                    if (seenIds.add(productId)) {
                        topIds.add(productId);
                    }
                }
            }
            
            System.out.println("🔥 Step 2 - With other order statuses: " + topIds.size() + " products");
        }
        
        // Bước 3: Nếu VẪN chưa đủ, lấy thêm từ các đơn hàng trạng thái khác (NEW, PREPARING, DELIVERING, CANCELED)
        if (topIds.size() < limit) {
            EnumSet<OrderStatus> remainingStatuses = EnumSet.of(
                OrderStatus.NEW, 
                OrderStatus.PREPARING,
                OrderStatus.DELIVERING,
                OrderStatus.CANCELED
            );
            var remainingRows = orderItemRepo.topBestSellersByShop(shopId, remainingStatuses, PageRequest.of(0, limit * 3));
            
            for (var row : remainingRows) {
                if (row.getProduct() != null && topIds.size() < limit) {
                    Long productId = row.getProduct().getId();
                    if (seenIds.add(productId)) {
                        topIds.add(productId);
                    }
                }
            }
            
            System.out.println("🔥 Step 3 - With remaining order statuses: " + topIds.size() + " products");
        }
        
        // Bước 4: Nếu VẪN chưa đủ, lấy sản phẩm chưa có đơn hàng nào (sản phẩm mới)
        if (topIds.size() < limit) {
            List<Product> noOrderProducts = productRepo.findAll().stream()
                    .filter(p -> p.getShop() != null && p.getShop().getId().equals(shopId))
                    .filter(p -> "AVAILABLE".equals(p.getStatus()))
                    .filter(p -> !seenIds.contains(p.getId()))
                    .sorted((p1, p2) -> {
                        if (p1.getCreatedAt() == null) return 1;
                        if (p2.getCreatedAt() == null) return -1;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    })
                    .limit(limit - topIds.size())
                    .toList();
            
            for (Product p : noOrderProducts) {
                topIds.add(p.getId());
            }
            
            System.out.println("🔥 Step 4 - Added products without orders: " + topIds.size() + " total products");
        }
        
        return topIds;
    }

    /** 
     * Tổng số lượng bán theo productId từ các đơn ở trạng thái DELIVERED 
     */
    private Map<Long, Integer> computeSoldCountMap(Long shopId) {
        var statuses = EnumSet.of(OrderStatus.DELIVERED);
        var rows = orderItemRepo.topBestSellersByShop(shopId, statuses, PageRequest.of(0, 1000));

        Map<Long, Integer> map = new HashMap<>();
        for (var r : rows) {
            var p = r.getProduct();
            if (p != null) {
                map.merge(p.getId(), r.getTotal().intValue(), Integer::sum);
            }
        }
        
        System.out.println("📊 Computed soldCount for " + map.size() + " products");
        return map;
    }
    
    /**
     * Tính rating trung bình cho từng product từ reviews APPROVED
     */
    private Map<Long, BigDecimal> computeRatingMap(Long shopId) {
        // Lấy tất cả products của shop
        List<Product> products = productRepo.findAll().stream()
                .filter(p -> p.getShop().getId().equals(shopId))
                .toList();
        
        Map<Long, BigDecimal> ratingMap = new HashMap<>();
        
        for (Product p : products) {
            Double avgRating = reviewRepo.avgRating(p.getId(), ReviewStatus.APPROVED);
            if (avgRating != null && avgRating > 0) {
                ratingMap.put(p.getId(), BigDecimal.valueOf(avgRating));
            }
        }
        
        System.out.println("⭐ Computed ratings for " + ratingMap.size() + " products");
        return ratingMap;
    }

    /** Sắp xếp theo thứ tự danh sách id đầu vào */
    private List<Product> orderByIds(List<Product> products, List<Long> ids) {
        Map<Long, Integer> pos = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) pos.put(ids.get(i), i);
        return products.stream()
                .sorted(Comparator.comparingInt(p -> pos.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    private ProductCardDTO toCard(Product p, Map<Long, Integer> soldMap, Map<Long, BigDecimal> ratingMap) {
        String url = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().get(0).getUrl()
                : "https://via.placeholder.com/300x300/f5f5f5/999999?text=No+Image";
        var images = List.of(new ProductImageDTO(url));

        // ✅ Lấy soldCount thực tế từ orders DELIVERED
        int sold = soldMap.getOrDefault(p.getId(), 0);
        
        // ✅ Lấy rating thực tế từ reviews APPROVED (không dùng p.getRatingAvg() nữa)
        BigDecimal rating = ratingMap.get(p.getId()); // null nếu chưa có đánh giá

        return new ProductCardDTO(
                p.getId(),
                p.getName(),
                p.getBasePrice(),
                rating,  // ✅ Rating thực tế từ reviews
                sold,    // ✅ Sold thực tế từ orders
                images
        );
    }
}
