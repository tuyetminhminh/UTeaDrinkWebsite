package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopBanner;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.entity.ShopSection;
import net.codejava.utea.manager.repository.ShopBannerRepository;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.repository.ShopSectionRepository;
import net.codejava.utea.order.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ShopBannerRepository bannerRepo;
    private final ShopSectionRepository sectionRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;
    private final net.codejava.utea.review.repository.ReviewRepository reviewRepo;

    // ==================== SHOP CRUD ====================

    /**
     * Đăng ký shop mới (1 manager chỉ đăng ký 1 lần)
     */
    @Transactional
    public ShopDTO createShop(Long managerId, ShopDTO shopDTO) {
        // Kiểm tra manager đã có shop chưa
        if (shopManagerRepo.existsByManager_Id(managerId)) {
            throw new RuntimeException("Manager đã đăng ký shop rồi!");
        }

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager không tồn tại"));

        // Tạo shop mới
        Shop shop = Shop.builder()
                .name(shopDTO.getName())
                .address(shopDTO.getAddress())
                .phone(shopDTO.getPhone())
                .status("OPEN")
                .build();
        shop = shopRepo.save(shop);

        // Gán manager cho shop
        ShopManager shopManager = ShopManager.builder()
                .shop(shop)
                .manager(manager)
                .build();
        shopManagerRepo.save(shopManager);

        return convertToDTO(shop, manager);
    }

    /**
     * Lấy thông tin shop của manager
     */
    @Transactional(readOnly = true)
    public ShopDTO getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        Shop shop = shopManager.getShop();
        User manager = shopManager.getManager();

        return convertToDTO(shop, manager);
    }

    /**
     * Cập nhật thông tin shop
     */
    @Transactional
    public ShopDTO updateShop(Long managerId, ShopDTO shopDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        Shop shop = shopManager.getShop();
        shop.setName(shopDTO.getName());
        shop.setAddress(shopDTO.getAddress());
        shop.setPhone(shopDTO.getPhone());
        shop.setStatus(shopDTO.getStatus());

        shop = shopRepo.save(shop);

        return convertToDTO(shop, shopManager.getManager());
    }

    // ==================== PUBLIC API ====================

    /**
     * Lấy tất cả banner ACTIVE của shop (dành cho khách hàng)
     */
    public List<ShopBannerDTO> getActiveBanners(Long shopId) {
        List<ShopBanner> banners = bannerRepo.findByShopIdAndActiveOrderBySortOrderAsc(shopId, true);
        return banners.stream()
                .map(this::convertBannerToDTO)
                .collect(Collectors.toList());
    }

    // ==================== BANNER CRUD ====================

    /**
     * Lấy tất cả banner của shop
     */
    @Transactional(readOnly = true)
    public List<ShopBannerDTO> getAllBanners(Long shopId) {
        return bannerRepo.findByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(this::convertBannerToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo banner mới
     */
    @Transactional
    public ShopBannerDTO createBanner(Long managerId, ShopBannerDTO bannerDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        boolean isActive = bannerDTO.isActive();

        ShopBanner banner = ShopBanner.builder()
                .shop(shopManager.getShop())
                .title(bannerDTO.getTitle())
                .imageUrl(bannerDTO.getImageUrl())
                .link(bannerDTO.getLink())
                .sortOrder(bannerDTO.getSortOrder() != null ? bannerDTO.getSortOrder() : 0)
                .active(isActive)
                .build();

        banner = bannerRepo.save(banner);

        return convertBannerToDTO(banner);
    }

    /**
     * Cập nhật banner
     */
    @Transactional
    public ShopBannerDTO updateBanner(Long managerId, Long bannerId, ShopBannerDTO bannerDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopBanner banner = bannerRepo.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        // Kiểm tra banner có thuộc shop của manager không
        if (!banner.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa banner này");
        }

        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setLink(bannerDTO.getLink());
        banner.setSortOrder(bannerDTO.getSortOrder());
        banner.setActive(bannerDTO.isActive());

        banner = bannerRepo.save(banner);

        return convertBannerToDTO(banner);
    }

    /**
     * Xóa banner
     */
    @Transactional
    public void deleteBanner(Long managerId, Long bannerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopBanner banner = bannerRepo.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        // Kiểm tra banner có thuộc shop của manager không
        if (!banner.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền xóa banner này");
        }

        bannerRepo.delete(banner);
    }

    // ==================== SECTION CRUD ====================

    /**
     * Lấy tất cả section của shop (cho manager)
     */
    @Transactional(readOnly = true)
    public List<ShopSectionDTO> getAllSections(Long shopId) {
        return sectionRepo.findByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(this::convertSectionToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy sections ACTIVE với products đã populate (cho customer)
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getActiveSectionsWithProducts(Long shopId) {
        List<ShopSection> sections = sectionRepo.findByShopIdAndIsActiveOrderBySortOrderAsc(shopId, true);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        return sections.stream().map(section -> {
            java.util.Map<String, Object> sectionData = new java.util.HashMap<>();
            sectionData.put("id", section.getId());
            sectionData.put("title", section.getTitle());
            sectionData.put("sectionType", section.getSectionType());
            sectionData.put("sortOrder", section.getSortOrder());
            
            // Parse contentJson to get limit
            int limit = 8; // default
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> content = mapper.readValue(section.getContentJson(), java.util.Map.class);
                if (content.containsKey("limit")) {
                    limit = (Integer) content.get("limit");
                }
            } catch (Exception e) {
                System.err.println("Error parsing contentJson: " + e.getMessage());
            }
            
            // Get products based on section type
            List<Product> products = getProductsForSection(shopId, section.getSectionType(), limit);
            sectionData.put("products", products);
            
            return sectionData;
        }).collect(Collectors.toList());
    }
    
    /**
     * Lấy products cho một section type
     */
    private List<Product> getProductsForSection(Long shopId, String sectionType, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        
        List<Long> productIds = switch (sectionType) {
            case "FEATURED" -> getFeaturedIds(shopId, limit); // Theo rating THỰC TẾ từ reviews (cao → thấp)
            case "NEW_ARRIVALS" -> productRepo.findNewestIds(shopId, pageable); // Sản phẩm mới
            case "TOP_SELLING" -> getTopSellingIds(shopId, limit); // Theo số bán thực tế từ orders
            case "PROMOTION" -> getFeaturedIds(shopId, limit); // Promotion dùng featured
            default -> List.of();
        };
        
        if (productIds.isEmpty()) {
            System.out.println("⚠️ No products found for section type: " + sectionType);
            return List.of();
        }
        
        // Get full products with images
        List<Product> products = productRepo.findByIdsWithImages(productIds);
        
        System.out.println("✅ Found " + products.size() + " products for section type: " + sectionType);
        
        // Sắp xếp lại theo thứ tự của productIds
        return orderProductsByIds(products, productIds);
    }
    
    /**
     * Lấy IDs sản phẩm nổi bật - SẮP XẾP THEO RATING, lấy đủ limit
     */
    private List<Long> getFeaturedIds(Long shopId, int limit) {
        // Tính rating map từ reviews APPROVED
        java.util.Map<Long, java.math.BigDecimal> ratingMap = new java.util.HashMap<>();
        
        List<Product> allProducts = productRepo.findAll().stream()
                .filter(p -> p.getShop() != null && p.getShop().getId().equals(shopId))
                .filter(p -> "AVAILABLE".equals(p.getStatus()))
                .toList();
        
        if (allProducts.isEmpty()) {
            return List.of();
        }
        
        // Tính rating cho từng product
        for (Product p : allProducts) {
            Double avgRating = reviewRepo.avgRating(p.getId(), net.codejava.utea.review.entity.enums.ReviewStatus.APPROVED);
            if (avgRating != null && avgRating > 0) {
                ratingMap.put(p.getId(), java.math.BigDecimal.valueOf(avgRating));
            }
        }
        
        // Bước 1: Lấy products CÓ RATING (cao → thấp)
        List<Long> featuredIds = new java.util.ArrayList<>();
        java.util.Set<Long> seenIds = new java.util.HashSet<>();
        
        allProducts.stream()
                .filter(p -> ratingMap.containsKey(p.getId()))
                .sorted((p1, p2) -> {
                    java.math.BigDecimal rating1 = ratingMap.get(p1.getId());
                    java.math.BigDecimal rating2 = ratingMap.get(p2.getId());
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
        
        // Bước 2: Nếu chưa đủ, lấy thêm products CHƯA CÓ RATING (mới nhất)
        if (featuredIds.size() < limit) {
            allProducts.stream()
                    .filter(p -> !seenIds.contains(p.getId()))
                    .sorted((p1, p2) -> {
                        if (p1.getCreatedAt() == null) return 1;
                        if (p2.getCreatedAt() == null) return -1;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    })
                    .limit(limit - featuredIds.size())
                    .forEach(p -> featuredIds.add(p.getId()));
            
            System.out.println("⭐ Featured Step 2: Total " + featuredIds.size() + " products");
        }
        
        return featuredIds;
    }
    
    /**
     * Lấy IDs sản phẩm bán chạy từ orders thực tế - ƯU TIÊN DELIVERED, lấy đủ limit
     */
    private List<Long> getTopSellingIds(Long shopId, int limit) {
        List<Long> topIds = new java.util.ArrayList<>();
        java.util.Set<Long> seenIds = new java.util.HashSet<>();
        
        // Bước 1: Lấy từ đơn DELIVERED
        java.util.EnumSet<net.codejava.utea.order.entity.enums.OrderStatus> deliveredStatuses = 
                java.util.EnumSet.of(net.codejava.utea.order.entity.enums.OrderStatus.DELIVERED);
        var deliveredRows = orderItemRepo.topBestSellersByShop(shopId, deliveredStatuses, 
                org.springframework.data.domain.PageRequest.of(0, limit * 2));
        
        for (var row : deliveredRows) {
            if (row.getProduct() != null && topIds.size() < limit) {
                Long productId = row.getProduct().getId();
                if (seenIds.add(productId)) {
                    topIds.add(productId);
                }
            }
        }
        
        System.out.println("🔥 Step 1 - From DELIVERED: " + topIds.size() + " products");
        
        // Bước 2: Nếu chưa đủ, lấy thêm từ các trạng thái khác
        if (topIds.size() < limit) {
            java.util.EnumSet<net.codejava.utea.order.entity.enums.OrderStatus> otherStatuses = 
                    java.util.EnumSet.of(
                        net.codejava.utea.order.entity.enums.OrderStatus.DELIVERING,
                        net.codejava.utea.order.entity.enums.OrderStatus.PAID,
                        net.codejava.utea.order.entity.enums.OrderStatus.CONFIRMED,
                        net.codejava.utea.order.entity.enums.OrderStatus.PREPARING
                    );
            var otherRows = orderItemRepo.topBestSellersByShop(shopId, otherStatuses,
                    org.springframework.data.domain.PageRequest.of(0, limit * 3));
            
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
        
        // Bước 3: Nếu VẪN chưa đủ, lấy từ đơn hàng trạng thái còn lại (NEW, PREPARING, DELIVERING, CANCELED)
        if (topIds.size() < limit) {
            java.util.EnumSet<net.codejava.utea.order.entity.enums.OrderStatus> remainingStatuses = 
                    java.util.EnumSet.of(
                        net.codejava.utea.order.entity.enums.OrderStatus.NEW,
                        net.codejava.utea.order.entity.enums.OrderStatus.PREPARING,
                        net.codejava.utea.order.entity.enums.OrderStatus.DELIVERING,
                        net.codejava.utea.order.entity.enums.OrderStatus.CANCELED
                    );
            var remainingRows = orderItemRepo.topBestSellersByShop(shopId, remainingStatuses,
                    org.springframework.data.domain.PageRequest.of(0, limit * 3));
            
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
        
        // Bước 4: Nếu VẪN chưa đủ, lấy sản phẩm chưa có đơn hàng (sản phẩm mới)
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
     * Sắp xếp products theo thứ tự của list IDs
     */
    private List<Product> orderProductsByIds(List<Product> products, List<Long> ids) {
        java.util.Map<Long, Integer> posMap = new java.util.HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            posMap.put(ids.get(i), i);
        }
        return products.stream()
                .sorted(java.util.Comparator.comparingInt(p -> posMap.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    /**
     * Tạo section mới
     */
    @Transactional
    public ShopSectionDTO createSection(Long managerId, ShopSectionDTO sectionDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        // Default to true if not specified
        Boolean isActive = sectionDTO.getIsActive() != null ? sectionDTO.getIsActive() : true;
        
        System.out.println("🔍 Creating section - DTO isActive: " + sectionDTO.getIsActive() + ", final value: " + isActive);

        ShopSection section = ShopSection.builder()
                .shop(shopManager.getShop())
                .title(sectionDTO.getTitle())
                .sectionType(sectionDTO.getSectionType())
                .contentJson(sectionDTO.getContentJson())
                .sortOrder(sectionDTO.getSortOrder() != null ? sectionDTO.getSortOrder() : 0)
                .isActive(isActive)
                .build();

        section = sectionRepo.save(section);
        
        System.out.println("✅ Section saved - ID: " + section.getId() + ", isActive in DB: " + section.isActive());

        return convertSectionToDTO(section);
    }

    /**
     * Cập nhật section
     */
    @Transactional
    public ShopSectionDTO updateSection(Long managerId, Long sectionId, ShopSectionDTO sectionDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại"));

        // Kiểm tra section có thuộc shop của manager không
        if (!section.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa section này");
        }

        Boolean isActive = sectionDTO.getIsActive() != null ? sectionDTO.getIsActive() : section.isActive();
        
        System.out.println("🔍 Updating section " + sectionId + " - DTO isActive: " + sectionDTO.getIsActive() + ", final value: " + isActive);

        section.setTitle(sectionDTO.getTitle());
        section.setSectionType(sectionDTO.getSectionType());
        section.setContentJson(sectionDTO.getContentJson());
        section.setSortOrder(sectionDTO.getSortOrder());
        section.setActive(isActive);

        section = sectionRepo.save(section);
        
        System.out.println("✅ Section updated - ID: " + section.getId() + ", isActive in DB: " + section.isActive());

        return convertSectionToDTO(section);
    }

    /**
     * Xóa section
     */
    @Transactional
    public void deleteSection(Long managerId, Long sectionId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại"));

        // Kiểm tra section có thuộc shop của manager không
        if (!section.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền xóa section này");
        }

        sectionRepo.delete(section);
    }

    // ==================== HELPER METHODS ====================

    private ShopDTO convertToDTO(Shop shop, User manager) {
        return ShopDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .address(shop.getAddress())
                .phone(shop.getPhone())
                .status(shop.getStatus())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .managerId(manager.getId())
                .managerName(manager.getFullName())
                .build();
    }

    private ShopBannerDTO convertBannerToDTO(ShopBanner banner) {
        return ShopBannerDTO.builder()
                .id(banner.getId())
                .shopId(banner.getShop().getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .link(banner.getLink())
                .sortOrder(banner.getSortOrder())
                .active(banner.isActive())
                .createdAt(banner.getCreatedAt())
                .build();
    }

    private ShopSectionDTO convertSectionToDTO(ShopSection section) {
        return ShopSectionDTO.builder()
                .id(section.getId())
                .shopId(section.getShop().getId())
                .title(section.getTitle())
                .sectionType(section.getSectionType())
                .contentJson(section.getContentJson())
                .sortOrder(section.getSortOrder())
                .isActive(section.isActive())
                .createdAt(section.getCreatedAt())
                .build();
    }

    /** Lấy toàn bộ banner ACTIVE (web chung, không theo shop) */
    @Transactional(readOnly = true)
    public List<ShopBannerDTO> getActiveBannersAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder")
                .and(Sort.by(Sort.Direction.DESC, "createdAt")); // tie-breaker đẹp
        return bannerRepo.findByActiveTrue(sort)
                .stream()
                .map(this::convertBannerToDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Optional<Long> getDefaultShopId() {
        return shopRepo.findFirstByStatusOrderByIdAsc("OPEN")
                .or(() -> shopRepo.findFirstByOrderByIdAsc())
                .map(Shop::getId);
    }

    @Transactional(readOnly = true)
    public Long requireDefaultShopId() {
        return getDefaultShopId()
                .orElseThrow(() -> new IllegalStateException("Chưa có Shop nào trong hệ thống."));
    }

}

