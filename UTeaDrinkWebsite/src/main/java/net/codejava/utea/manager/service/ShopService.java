package net.codejava.utea.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ShopBannerRepository bannerRepo;
    private final ShopSectionRepository sectionRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;

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
    public List<Map<String, Object>> getActiveSectionsWithProducts(Long shopId) {
        List<ShopSection> sections = sectionRepo.findByShopIdAndIsActiveOrderBySortOrderAsc(shopId, true);
        
        return sections.stream().map(section -> {
            Map<String, Object> sectionData = new HashMap<>();
            sectionData.put("id", section.getId());
            sectionData.put("title", section.getTitle());
            sectionData.put("sectionType", section.getSectionType());
            sectionData.put("sortOrder", section.getSortOrder());
            
            // Parse contentJson to get limit
            int limit = 8; // default
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = objectMapper.readValue(section.getContentJson(), Map.class);
                if (content.containsKey("limit")) {
                    limit = (Integer) content.get("limit");
                }
            } catch (Exception e) {
                System.err.println("Error parsing contentJson: " + e.getMessage());
            }
            
            // Get products based on section type and convert to simple DTOs
            List<Map<String, Object>> products = getProductsForSection(shopId, section.getSectionType(), limit);
            sectionData.put("products", products);
            
            return sectionData;
        }).collect(Collectors.toList());
    }
    
    /**
     * Lấy products cho một section type và convert sang Map để tránh lazy loading issues
     */
    private List<Map<String, Object>> getProductsForSection(Long shopId, String sectionType, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        
        List<Product> products = switch (sectionType) {
            case "FEATURED" -> productRepo.findFeaturedProducts(shopId, pageRequest);
            case "NEW_ARRIVALS" -> productRepo.findNewArrivals(shopId, pageRequest);
            case "TOP_SELLING" -> productRepo.findTopSelling(shopId, pageRequest);
            case "PROMOTION" -> getPromotionProductsWithFallback(shopId, limit);
            default -> List.of();
        };
        
        // Convert to simple DTO to avoid lazy loading issues
        return products.stream().map(this::convertProductToSimpleDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy sản phẩm khuyến mãi với fallback:
     * - Ưu tiên: Sản phẩm có rating >= 4.0 và bán chạy
     * - Nếu không đủ: Bổ sung thêm sản phẩm bán chạy khác để đủ số lượng
     */
    private List<Product> getPromotionProductsWithFallback(Long shopId, int limit) {
        // Bước 1: Lấy sản phẩm có rating >= 4.0
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Product> highRatingProducts = productRepo.findPromotionProducts(shopId, pageRequest);
        
        // Nếu đã đủ số lượng, return luôn
        if (highRatingProducts.size() >= limit) {
            return highRatingProducts;
        }
        
        // Bước 2: Nếu chưa đủ, lấy thêm sản phẩm bán chạy để bù đủ
        int remaining = limit - highRatingProducts.size();
        PageRequest fallbackPageRequest = PageRequest.of(0, limit); // Lấy nhiều hơn để filter
        List<Product> topSellingProducts = productRepo.findTopSelling(shopId, fallbackPageRequest);
        
        // Lọc bỏ các sản phẩm đã có trong highRatingProducts
        java.util.Set<Long> existingIds = highRatingProducts.stream()
                .map(Product::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        List<Product> additionalProducts = topSellingProducts.stream()
                .filter(p -> !existingIds.contains(p.getId()))
                .limit(remaining)
                .collect(java.util.stream.Collectors.toList());
        
        // Kết hợp 2 danh sách
        List<Product> result = new ArrayList<>(highRatingProducts);
        result.addAll(additionalProducts);
        
        return result;
    }
    
    /**
     * Convert Product entity to simple DTO for JSON response
     */
    private Map<String, Object> convertProductToSimpleDTO(Product product) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", product.getId());
        dto.put("name", product.getName());
        dto.put("description", product.getDescription());
        dto.put("basePrice", product.getBasePrice());
        
        // Tính số lượng đã bán thực tế từ các đơn hàng DELIVERED
        int actualSoldCount = calculateActualSoldCount(product.getId());
        dto.put("soldCount", actualSoldCount);
        
        // Tính rating trung bình thực tế từ reviews APPROVED
        BigDecimal actualRating = calculateAverageRating(product.getId());
        dto.put("ratingAvg", actualRating);
        
        dto.put("status", product.getStatus());
        
        // Get images safely
        List<Map<String, Object>> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (net.codejava.utea.catalog.entity.ProductImage img : product.getImages()) {
                Map<String, Object> imageDto = new HashMap<>();
                imageDto.put("id", img.getId());
                imageDto.put("url", img.getUrl());
                imageDto.put("sortOrder", img.getSortOrder());
                images.add(imageDto);
            }
        }
        dto.put("images", images);
        
        return dto;
    }
    
    /**
     * Tính số lượng sản phẩm đã bán thực tế từ các đơn hàng DELIVERED
     */
    private int calculateActualSoldCount(Long productId) {
        try {
            // Lấy tất cả đơn hàng DELIVERED
            List<Order> deliveredOrders = orderRepo.findAll().stream()
                    .filter(order -> OrderStatus.DELIVERED.equals(order.getStatus()))
                    .collect(Collectors.toList());
            
            // Tính tổng số lượng đã bán
            int totalSold = 0;
            for (Order order : deliveredOrders) {
                List<OrderItem> items = order.getItems();
                if (items != null) {
                    for (OrderItem item : items) {
                        if (item.getProduct() != null && item.getProduct().getId().equals(productId)) {
                            totalSold += item.getQuantity();
                        }
                    }
                }
            }
            
            return totalSold;
        } catch (Exception e) {
            System.err.println("Error calculating sold count for product " + productId + ": " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Tính rating trung bình thực tế từ reviews APPROVED
     */
    private BigDecimal calculateAverageRating(Long productId) {
        try {
            Double avgRating = reviewRepo.avgRating(productId, ReviewStatus.APPROVED);
            if (avgRating == null || avgRating == 0.0) {
                return null; // Trả về null nếu chưa có đánh giá
            }
            return BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.err.println("Error calculating rating for product " + productId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo section mới
     */
    @Transactional
    public ShopSectionDTO createSection(Long managerId, ShopSectionDTO sectionDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        // Debug logging
        System.out.println("=== CREATE SECTION DEBUG ===");
        System.out.println("DTO isActive: " + sectionDTO.getIsActive());
        System.out.println("DTO class: " + sectionDTO.getClass().getName());

        // Default to true if not specified
        Boolean isActive = sectionDTO.getIsActive() != null ? sectionDTO.getIsActive() : true;
        System.out.println("isActive value after default: " + isActive);

        ShopSection section = ShopSection.builder()
                .shop(shopManager.getShop())
                .title(sectionDTO.getTitle())
                .sectionType(sectionDTO.getSectionType())
                .contentJson(sectionDTO.getContentJson())
                .sortOrder(sectionDTO.getSortOrder() != null ? sectionDTO.getSortOrder() : 0)
                .isActive(isActive)
                .build();

        System.out.println("Entity isActive before save: " + section.isActive());
        
        section = sectionRepo.save(section);

        System.out.println("Entity isActive after save: " + section.isActive());
        System.out.println("=== END DEBUG ===");

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

        // Debug logging
        System.out.println("=== UPDATE SECTION DEBUG ===");
        System.out.println("DTO isActive: " + sectionDTO.getIsActive());
        
        Boolean isActive = sectionDTO.getIsActive() != null ? sectionDTO.getIsActive() : true;
        System.out.println("isActive value after default: " + isActive);

        section.setTitle(sectionDTO.getTitle());
        section.setSectionType(sectionDTO.getSectionType());
        section.setContentJson(sectionDTO.getContentJson());
        section.setSortOrder(sectionDTO.getSortOrder());
        section.setActive(isActive);

        System.out.println("Entity isActive before save: " + section.isActive());
        
        section = sectionRepo.save(section);

        System.out.println("Entity isActive after save: " + section.isActive());
        System.out.println("=== END DEBUG ===");

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
}

