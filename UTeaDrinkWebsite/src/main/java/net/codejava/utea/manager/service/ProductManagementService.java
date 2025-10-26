package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.*;
import net.codejava.utea.catalog.entity.enums.Size;
import net.codejava.utea.catalog.repository.*;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.media.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductImageRepository imageRepo;
    private final ProductVariantRepository variantRepo;
    private final ToppingRepository toppingRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final CloudinaryService cloudinaryService;
    private final net.codejava.utea.order.repository.OrderRepository orderRepo;

    // ==================== PRODUCT CRUD ====================

    /**
     * Lấy tất cả sản phẩm của shop (phân trang + filter + sort)
     */
    @Transactional(readOnly = true)
    public Page<ProductManagementDTO> getAllProducts(Long managerId, Pageable pageable, 
                                                      String search, Long categoryId, String status, String sortBy) {
        Shop shop = getShopByManagerId(managerId);
        
        System.out.println("=== DEBUG: getAllProducts ===");
        System.out.println("Manager ID: " + managerId);
        System.out.println("Shop ID: " + shop.getId());
        System.out.println("Search: " + search);
        System.out.println("CategoryId: " + categoryId);
        System.out.println("Status: " + status);
        System.out.println("SortBy: " + sortBy);
        
        // Lấy TẤT CẢ sản phẩm của shop này
        List<Product> allShopProducts = productRepo.findAll().stream()
                .filter(product -> product.getShop() != null && product.getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());
        
        System.out.println("Total shop products before filter: " + allShopProducts.size());
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            allShopProducts = allShopProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
            System.out.println("After search filter: " + allShopProducts.size());
        }
        
        if (categoryId != null) {
            allShopProducts = allShopProducts.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
            System.out.println("After category filter: " + allShopProducts.size());
        }
        
        if (status != null && !status.trim().isEmpty()) {
            allShopProducts = allShopProducts.stream()
                    .filter(p -> status.equals(p.getStatus()))
                    .collect(Collectors.toList());
            System.out.println("After status filter: " + allShopProducts.size());
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            allShopProducts = applySorting(allShopProducts, sortBy);
            System.out.println("After sorting by: " + sortBy);
        }
        
        // Áp dụng pagination TRÊN kết quả đã filter
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allShopProducts.size());
        List<Product> pageContent = start < allShopProducts.size() ? 
                                     allShopProducts.subList(start, end) : 
                                     List.of();
        
        // Convert to Page
        return new org.springframework.data.domain.PageImpl<>(
                pageContent.stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                allShopProducts.size()
        );
    }
    
    /**
     * Áp dụng sắp xếp cho danh sách sản phẩm
     */
    private List<Product> applySorting(List<Product> products, String sortBy) {
        switch (sortBy) {
            case "newest":
                // Sắp xếp theo mới nhất
                return products.stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .collect(Collectors.toList());
                
            case "best_selling":
                // Sắp xếp theo bán chạy nhất (từ orders DELIVERED)
                // Lấy tất cả orders DELIVERED một lần
                List<net.codejava.utea.order.entity.Order> deliveredOrders = orderRepo.findAll().stream()
                        .filter(order -> net.codejava.utea.order.entity.enums.OrderStatus.DELIVERED.equals(order.getStatus()))
                        .collect(Collectors.toList());
                
                // Tính sold count cho từng sản phẩm
                java.util.Map<Long, Integer> soldCountMap = new java.util.HashMap<>();
                for (net.codejava.utea.order.entity.Order order : deliveredOrders) {
                    if (order.getItems() != null) {
                        for (net.codejava.utea.order.entity.OrderItem item : order.getItems()) {
                            if (item.getProduct() != null) {
                                Long productId = item.getProduct().getId();
                                soldCountMap.put(productId, 
                                    soldCountMap.getOrDefault(productId, 0) + item.getQuantity());
                            }
                        }
                    }
                }
                
                // Sắp xếp theo sold count từ cao đến thấp
                return products.stream()
                        .sorted((p1, p2) -> Integer.compare(
                            soldCountMap.getOrDefault(p2.getId(), 0),
                            soldCountMap.getOrDefault(p1.getId(), 0)
                        ))
                        .collect(Collectors.toList());
                
            default:
                // Mặc định: không sắp xếp hoặc giữ nguyên
                return products;
        }
    }

    /**
     * Lấy chi tiết sản phẩm
     */
    @Transactional(readOnly = true)
    public ProductManagementDTO getProductById(Long managerId, Long productId) {
        Shop shop = getShopByManagerId(managerId);
        
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền truy cập sản phẩm này");
        }

        return convertToDTO(product);
    }

    /**
     * Tạo sản phẩm mới
     */
    @Transactional
    public ProductManagementDTO createProduct(Long managerId, ProductManagementDTO productDTO) {
        Shop shop = getShopByManagerId(managerId);

        ProductCategory category = categoryRepo.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .basePrice(productDTO.getBasePrice())
                .soldCount(0)
                .ratingAvg(BigDecimal.ZERO)
                .status("AVAILABLE")
                .build();

        product = productRepo.save(product);

        return convertToDTO(product);
    }

    /**
     * Cập nhật sản phẩm
     */
    @Transactional
    public ProductManagementDTO updateProduct(Long managerId, Long productId, ProductManagementDTO productDTO) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa sản phẩm này");
        }

        if (productDTO.getCategoryId() != null) {
            ProductCategory category = categoryRepo.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setStatus(productDTO.getStatus());

        product = productRepo.save(product);

        return convertToDTO(product);
    }

    /**
     * Xóa sản phẩm
     */
    @Transactional
    public void deleteProduct(Long managerId, Long productId) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa sản phẩm này");
        }

        productRepo.delete(product);
    }

    // ==================== PRODUCT IMAGE ====================

    /**
     * Upload ảnh sản phẩm
     */
    @Transactional
    public ProductImageDTO uploadImage(Long managerId, Long productId, MultipartFile file, Integer sortOrder) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa sản phẩm này");
        }

        // Upload lên Cloudinary
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinaryService.upload(file);
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(imageUrl)
                    .publicId(publicId)
                    .sortOrder(sortOrder != null ? sortOrder : 0)
                    .build();

            image = imageRepo.save(image);

            return convertImageToDTO(image);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    /**
     * Thêm ảnh từ URL
     */
    @Transactional
    public ProductImageDTO addImageFromUrl(Long managerId, Long productId, String imageUrl) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa sản phẩm này");
        }

        // Lấy sortOrder tiếp theo
        int nextSortOrder = product.getImages() != null ? product.getImages().size() : 0;

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .publicId(null) // URL không có publicId
                .sortOrder(nextSortOrder)
                .build();

        image = imageRepo.save(image);

        return convertImageToDTO(image);
    }

    /**
     * Xóa ảnh sản phẩm
     */
    @Transactional
    public void deleteImage(Long managerId, Long imageId) {
        Shop shop = getShopByManagerId(managerId);

        ProductImage image = imageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Ảnh không tồn tại"));

        // Kiểm tra ảnh có thuộc sản phẩm của shop manager không
        if (!image.getProduct().getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa ảnh này");
        }

        // Xóa ảnh trên Cloudinary (nếu có publicId)
        if (image.getPublicId() != null) {
            try {
                cloudinaryService.delete(image.getPublicId());
            } catch (Exception e) {
                // Log error but continue
            }
        }

        imageRepo.delete(image);
    }

    // ==================== PRODUCT VARIANT ====================

    /**
     * Thêm biến thể cho sản phẩm
     */
    @Transactional
    public ProductVariantDTO addVariant(Long managerId, Long productId, ProductVariantDTO variantDTO) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa sản phẩm này");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(Size.valueOf(variantDTO.getSize()))
                .price(variantDTO.getPrice())
                .volumeMl(variantDTO.getVolumeMl())
                .build();

        variant = variantRepo.save(variant);

        return convertVariantToDTO(variant);
    }

    /**
     * Cập nhật biến thể
     */
    @Transactional
    public ProductVariantDTO updateVariant(Long managerId, Long variantId, ProductVariantDTO variantDTO) {
        Shop shop = getShopByManagerId(managerId);

        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        // Kiểm tra biến thể có thuộc shop của manager không
        if (!variant.getProduct().getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa biến thể này");
        }

        variant.setSize(Size.valueOf(variantDTO.getSize()));
        variant.setPrice(variantDTO.getPrice());
        variant.setVolumeMl(variantDTO.getVolumeMl());

        variant = variantRepo.save(variant);

        return convertVariantToDTO(variant);
    }

    /**
     * Xóa biến thể
     */
    @Transactional
    public void deleteVariant(Long managerId, Long variantId) {
        Shop shop = getShopByManagerId(managerId);

        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        // Kiểm tra biến thể có thuộc shop của manager không
        if (!variant.getProduct().getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa biến thể này");
        }

        variantRepo.delete(variant);
    }

    // ==================== TOPPING CRUD ====================

    /**
     * Lấy tất cả topping của shop
     */
    public List<ToppingDTO> getAllToppings(Long managerId) {
        Shop shop = getShopByManagerId(managerId);
        
        return toppingRepo.findByShopId(shop.getId()).stream()
                .map(this::convertToppingToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo topping mới
     */
    @Transactional
    public ToppingDTO createTopping(Long managerId, ToppingDTO toppingDTO) {
        Shop shop = getShopByManagerId(managerId);

        Topping topping = Topping.builder()
                .shop(shop)
                .name(toppingDTO.getName())
                .price(toppingDTO.getPrice())
                .status("ACTIVE")
                .build();

        topping = toppingRepo.save(topping);

        return convertToppingToDTO(topping);
    }

    /**
     * Cập nhật topping
     */
    @Transactional
    public ToppingDTO updateTopping(Long managerId, Long toppingId, ToppingDTO toppingDTO) {
        Shop shop = getShopByManagerId(managerId);

        Topping topping = toppingRepo.findById(toppingId)
                .orElseThrow(() -> new RuntimeException("Topping không tồn tại"));

        // Kiểm tra topping có thuộc shop của manager không
        if (!topping.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa topping này");
        }

        topping.setName(toppingDTO.getName());
        topping.setPrice(toppingDTO.getPrice());
        topping.setStatus(toppingDTO.getStatus());

        topping = toppingRepo.save(topping);

        return convertToppingToDTO(topping);
    }

    /**
     * Xóa topping
     */
    @Transactional
    public void deleteTopping(Long managerId, Long toppingId) {
        Shop shop = getShopByManagerId(managerId);

        Topping topping = toppingRepo.findById(toppingId)
                .orElseThrow(() -> new RuntimeException("Topping không tồn tại"));

        // Kiểm tra topping có thuộc shop của manager không
        if (!topping.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa topping này");
        }

        toppingRepo.delete(topping);
    }

    /**
     * Toggle topping status (ACTIVE <-> HIDDEN)
     */
    @Transactional
    public ToppingDTO toggleToppingStatus(Long managerId, Long toppingId) {
        Shop shop = getShopByManagerId(managerId);
        
        Topping topping = toppingRepo.findById(toppingId)
                .orElseThrow(() -> new RuntimeException("Topping không tồn tại"));
        
        // Check if topping belongs to manager's shop
        if (!topping.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền thao tác topping này");
        }
        
        // Toggle status
        if ("ACTIVE".equals(topping.getStatus())) {
            topping.setStatus("HIDDEN");
        } else {
            topping.setStatus("ACTIVE");
        }
        
        topping = toppingRepo.save(topping);
        
        return convertToppingToDTO(topping);
    }

    // ==================== STATS & UTILITIES ====================

    /**
     * Lấy thống kê sản phẩm
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductStats(Long managerId) {
        Shop shop = getShopByManagerId(managerId);
        
        List<Product> allProducts = productRepo.findAll().stream()
                .filter(p -> p.getShop() != null && p.getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());
        
        long total = allProducts.size();
        long available = allProducts.stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus()))
                .count();
        long hidden = allProducts.stream()
                .filter(p -> "HIDDEN".equals(p.getStatus()))
                .count();
        
        // Tìm sản phẩm bán chạy nhất (tính từ orders DELIVERED)
        String bestSeller = "--";
        if (!allProducts.isEmpty()) {
            // Lấy tất cả orders DELIVERED một lần
            List<net.codejava.utea.order.entity.Order> deliveredOrders = orderRepo.findAll().stream()
                    .filter(order -> net.codejava.utea.order.entity.enums.OrderStatus.DELIVERED.equals(order.getStatus()))
                    .collect(Collectors.toList());
            
            // Tính sold count cho từng sản phẩm
            java.util.Map<Long, Integer> soldCountMap = new java.util.HashMap<>();
            for (net.codejava.utea.order.entity.Order order : deliveredOrders) {
                if (order.getItems() != null) {
                    for (net.codejava.utea.order.entity.OrderItem item : order.getItems()) {
                        if (item.getProduct() != null) {
                            Long productId = item.getProduct().getId();
                            soldCountMap.put(productId, 
                                soldCountMap.getOrDefault(productId, 0) + item.getQuantity());
                        }
                    }
                }
            }
            
            // Tìm sản phẩm có sold count cao nhất
            bestSeller = allProducts.stream()
                    .max((p1, p2) -> Integer.compare(
                        soldCountMap.getOrDefault(p1.getId(), 0),
                        soldCountMap.getOrDefault(p2.getId(), 0)
                    ))
                    .map(Product::getName)
                    .orElse("--");
        }
        
        return Map.of(
            "total", total,
            "available", available,
            "hidden", hidden,
            "bestSeller", bestSeller
        );
    }

    /**
     * Lấy danh sách tất cả categories
     */
    public List<Map<String, Object>> getAllCategories() {
        return categoryRepo.findAll().stream()
                .map(cat -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", cat.getId());
                    map.put("name", cat.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi trạng thái sản phẩm (AVAILABLE <-> HIDDEN)
     */
    @Transactional
    public ProductManagementDTO toggleProductStatus(Long managerId, Long productId) {
        Shop shop = getShopByManagerId(managerId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra sản phẩm có thuộc shop của manager không
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền thao tác sản phẩm này");
        }

        // Toggle status: AVAILABLE <-> HIDDEN
        if ("AVAILABLE".equals(product.getStatus())) {
            product.setStatus("HIDDEN");
        } else {
            product.setStatus("AVAILABLE");
        }

        product = productRepo.save(product);

        return convertToDTO(product);
    }

    // ==================== HELPER METHODS ====================

    @Transactional(readOnly = true)
    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    private ProductManagementDTO convertToDTO(Product product) {
        try {
            // Tính số lượng đã bán thực tế từ các đơn hàng DELIVERED
            int actualSoldCount = calculateActualSoldCount(product.getId());
            
            return ProductManagementDTO.builder()
                    .id(product.getId())
                    .shopId(product.getShop().getId())
                    .shopName(product.getShop().getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                    .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                    .name(product.getName())
                    .description(product.getDescription())
                    .basePrice(product.getBasePrice())
                    .soldCount(actualSoldCount)  // Sử dụng số lượng thực tế
                    .ratingAvg(product.getRatingAvg())
                    .status(product.getStatus())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .images(product.getImages() != null ? 
                            product.getImages().stream().map(this::convertImageToDTO).collect(Collectors.toList()) : 
                            List.of())
                    .variants(product.getVariants() != null ? 
                            product.getVariants().stream().map(this::convertVariantToDTO).collect(Collectors.toList()) : 
                            List.of())
                    .build();
        } catch (Exception e) {
            System.err.println("Error converting product to DTO: " + e.getMessage());
            // Return basic DTO without images/variants if lazy loading fails
            int actualSoldCount = calculateActualSoldCount(product.getId());
            
        return ProductManagementDTO.builder()
                .id(product.getId())
                .shopId(product.getShop().getId())
                .shopName(product.getShop().getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                    .soldCount(actualSoldCount)  // Sử dụng số lượng thực tế
                .ratingAvg(product.getRatingAvg())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                    .images(List.of())
                    .variants(List.of())
                .build();
        }
    }

    /**
     * Tính số lượng sản phẩm đã bán thực tế từ các đơn hàng DELIVERED
     */
    @Transactional(readOnly = true)
    private int calculateActualSoldCount(Long productId) {
        try {
            System.out.println("=== DEBUG: calculateActualSoldCount for product " + productId + " ===");
            
            // Lấy tất cả đơn hàng
            List<net.codejava.utea.order.entity.Order> allOrders = orderRepo.findAll();
            System.out.println("Total orders in DB: " + allOrders.size());
            
            // Lọc orders DELIVERED (sử dụng enum OrderStatus.DELIVERED)
            List<net.codejava.utea.order.entity.Order> deliveredOrders = allOrders.stream()
                    .filter(order -> net.codejava.utea.order.entity.enums.OrderStatus.DELIVERED.equals(order.getStatus()))
                    .collect(Collectors.toList());
            System.out.println("Delivered orders: " + deliveredOrders.size());
            
            // Tính tổng số lượng đã bán
            int totalSold = 0;
            for (net.codejava.utea.order.entity.Order order : deliveredOrders) {
                List<net.codejava.utea.order.entity.OrderItem> items = order.getItems();
                if (items != null) {
                    for (net.codejava.utea.order.entity.OrderItem item : items) {
                        if (item.getProduct() != null && item.getProduct().getId().equals(productId)) {
                            totalSold += item.getQuantity();
                            System.out.println("  Order #" + order.getId() + " - Item quantity: " + item.getQuantity());
                        }
                    }
                }
            }
            
            System.out.println("Total sold for product " + productId + ": " + totalSold);
            System.out.println("=== END DEBUG ===");
            
            return totalSold;
        } catch (Exception e) {
            System.err.println("Error calculating sold count for product " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private ProductImageDTO convertImageToDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .productId(image.getProduct().getId())
                .url(image.getUrl())
                .publicId(image.getPublicId())
                .sortOrder(image.getSortOrder())
                .build();
    }

    private ProductVariantDTO convertVariantToDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .size(variant.getSize().name())
                .price(variant.getPrice())
                .volumeMl(variant.getVolumeMl())
                .build();
    }

    private ToppingDTO convertToppingToDTO(Topping topping) {
        return ToppingDTO.builder()
                .id(topping.getId())
                .name(topping.getName())
                .price(topping.getPrice())
                .status(topping.getStatus())
                .build();
    }

}

