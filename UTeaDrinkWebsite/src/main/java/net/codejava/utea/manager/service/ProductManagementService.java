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

    // ==================== PRODUCT CRUD ====================

    /**
     * Lấy tất cả sản phẩm của shop (phân trang)
     */
    public Page<ProductManagementDTO> getAllProducts(Long managerId, Pageable pageable) {
        // Get shop to verify manager has shop
        getShopByManagerId(managerId);
        
        Page<Product> products = productRepo.findAll(pageable);
        
        return products.map(this::convertToDTO);
    }

    /**
     * Lấy chi tiết sản phẩm
     */
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

        // Xóa ảnh trên Cloudinary
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

    // ==================== HELPER METHODS ====================

    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManagerId(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    private ProductManagementDTO convertToDTO(Product product) {
        return ProductManagementDTO.builder()
                .id(product.getId())
                .shopId(product.getShop().getId())
                .shopName(product.getShop().getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .soldCount(product.getSoldCount())
                .ratingAvg(product.getRatingAvg())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .images(product.getImages().stream().map(this::convertImageToDTO).collect(Collectors.toList()))
                .variants(product.getVariants().stream().map(this::convertVariantToDTO).collect(Collectors.toList()))
                .build();
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
                .shopId(topping.getShop().getId())
                .name(topping.getName())
                .price(topping.getPrice())
                .status(topping.getStatus())
                .build();
    }
}

