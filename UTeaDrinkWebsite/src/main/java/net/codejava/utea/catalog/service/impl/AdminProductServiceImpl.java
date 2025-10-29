//package net.codejava.utea.catalog.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import net.codejava.utea.admin.controller.AdminCatalogController.ProductForm;
//import net.codejava.utea.admin.controller.AdminCatalogController.VariantForm;
//import net.codejava.utea.catalog.entity.Product;
//import net.codejava.utea.catalog.entity.ProductCategory;
//import net.codejava.utea.catalog.entity.ProductImage;
//import net.codejava.utea.catalog.entity.ProductVariant;
//import net.codejava.utea.catalog.repository.ProductCategoryRepository;
//import net.codejava.utea.catalog.repository.ProductImageRepository;
//import net.codejava.utea.catalog.repository.ProductRepository;
//import net.codejava.utea.catalog.repository.ProductVariantRepository;
//import net.codejava.utea.catalog.service.AdminProductService;
//import net.codejava.utea.manager.entity.Shop;
//import net.codejava.utea.manager.repository.ShopRepository;
//import net.codejava.utea.media.service.CloudinaryService;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.transaction.Transactional;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.text.Normalizer;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class AdminProductServiceImpl implements AdminProductService {
//
//    private final ProductRepository productRepo;
//    private final ProductCategoryRepository categoryRepo; // nếu chưa có, tạo JpaRepository<ProductCategory, Long>
//
//    private final ShopRepository shopRepo;
//
//    private final ProductImageRepository productImageRepo;
//    private final CloudinaryService cloudinaryService;
//    private final ProductVariantRepository variantRepo;
//    private final ProductImageRepository imageRepo;
//
//    @Override
//    @Transactional
//    public Page<Product> search(String q, Long shopId, String status, Pageable pageable) {
//        String kw = (q == null || q.isBlank()) ? null : q.trim();
//        String st = (status == null || status.isBlank()) ? "ALL" : status.trim();
//
//        Page<Product> page = productRepo.adminSearch(kw, shopId, st, pageable);
//
//        // Ép initialize trước khi trả ra view (vẫn còn trong @Transactional)
//        page.getContent().forEach(p -> {
//            // shop name (tránh lazy khi th:text="${p.shop.name}")
//            if (p.getShop() != null) {
//                // chạm getter để init
//                p.getShop().getName();
//            }
//            // images (tránh lazy khi gọi getMainImageUrl())
//            if (p.getImages() != null) {
//                p.getImages().size();
//            }
//        });
//
//        return page;
//    }
//
//    @Override
//    @Transactional
//    public void hide(Long id, String reason) {
//        productRepo.findById(id).ifPresent(p -> {
//            p.setStatus("HIDDEN");
//            // TODO: nếu muốn lưu lý do, tạo bảng log; ở đây chỉ set status
//        });
//    }
//
//    @Override
//    @Transactional
//    public void unhide(Long id) {
//        productRepo.findById(id).ifPresent(p -> p.setStatus("AVAILABLE"));
//        // hoặc productRepo.updateStatus(id, "AVAILABLE");
//    }
//
//    @Override
//    @Transactional
//    public void hardDelete(Long id) {
//        productRepo.findById(id).ifPresent(p -> {
//            if (p.getImages() != null) {
//                p.getImages().forEach(img -> {
//                    try {
//                        String pid = img.getPublicId();
//                        if (pid != null && !pid.isBlank()) {
//                            cloudinaryService.delete(pid);
//                        }
//                    } catch (Exception ex) {
//                        // log warn nhưng KHÔNG để văng 500
//                        // log.warn("Cloudinary delete fail: {}", img.getPublicId(), ex);
//                    }
//                });
//            }
//            productRepo.delete(p); // cascade ALL + orphanRemoval dọn product_images
//        });
//    }
//
//    @Override
//    @Transactional
//    public void updateBasic(Long id, String name, String description, BigDecimal basePrice, Long categoryId) {
//        productRepo.findById(id).ifPresent(p -> {
//            if (name != null && !name.isBlank())
//                p.setName(name.trim());
//            if (description != null)
//                p.setDescription(description.trim());
//            if (basePrice != null)
//                p.setBasePrice(basePrice);
//            if (categoryId != null) {
//                ProductCategory cat = categoryRepo.findById(categoryId).orElse(null);
//                p.setCategory(cat);
//            }
//        });
//    }
//
//    @Override
//    public Optional<Product> findById(Long id) {
//        return productRepo.findById(id);
//    }
//
//    @Override
//    public void create(String name, String description, BigDecimal basePrice, Long categoryId, Long shopId,
//                       String status, MultipartFile[] images) {
//        var shop = shopRepo.findById(shopId).orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
//        var category = (categoryId == null) ? null : categoryRepo.findById(categoryId).orElse(null);
//
//        Product p = new Product();
//        p.setName(name.trim());
//        p.setDescription(description);
//        p.setBasePrice(basePrice);
//        p.setStatus(status);
//        p.setShop(shop);
//        p.setCategory(category);
//        p = productRepo.save(p); // cần id để gắn ảnh
//
//        saveImages(p, images);
//    }
//
//    @Transactional
//    @Override
//    public void update(Long id, String name, String description, BigDecimal basePrice, Long categoryId, Long shopId,
//                       String status, MultipartFile[] newImages) {
//        var p = productRepo.findById(id).orElseThrow();
//        var shop = shopRepo.findById(shopId).orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
//        var category = (categoryId == null) ? null : categoryRepo.findById(categoryId).orElse(null);
//
//        p.setName(name.trim());
//        p.setDescription(description);
//        p.setBasePrice(basePrice);
//        p.setStatus(status);
//        p.setShop(shop);
//        p.setCategory(category);
//
//        // Nếu có ảnh mới, thêm tiếp (không bắt buộc xoá ảnh cũ)
//        if (newImages != null) {
//            saveImages(p, newImages);
//        }
//    }
//
//    private static String slugify(String input) {
//        if (input == null) return "image";
//        String n = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
//                .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // bỏ dấu
//        n = n.replaceAll("[^\\p{Alnum}]+", "-").replaceAll("(^-|-$)", "").toLowerCase();
//        return n.isBlank() ? "image" : n;
//    }
//
//    @SuppressWarnings("unchecked")
//    private void saveImages(Product p, MultipartFile[] images) {
//        if (images == null) return;
//
//        int nextOrder = productImageRepo.findMaxSortOrderByProductId(p.getId()).orElse(0) + 1;
//        String nameSlug = slugify(p.getName());
//        String folder = "products/" + p.getId();    // → sẽ thành utea/products/{id}
//
//        for (MultipartFile file : images) {
//            if (file == null || file.isEmpty()) continue;
//
//            String ts = java.time.LocalDateTime.now()
//                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//            String publicId = nameSlug + "_" + ts + "_" + nextOrder;  // ví dụ: tra-sua-matcha_20251025...
//
//            Map<String, Object> res;
//            try {
//                res = cloudinaryService.upload(file, folder, publicId);
//            } catch (java.io.IOException e) {
//                throw new RuntimeException("Upload ảnh thất bại cho sản phẩm ID=" + p.getId(), e);
//            }
//
//            String url = res.get("secure_url") != null
//                    ? res.get("secure_url").toString()
//                    : (res.get("url") != null ? res.get("url").toString() : null);
//            String pid = res.get("public_id") != null ? res.get("public_id").toString() : publicId;
//
//            ProductImage pi = new ProductImage();
//            pi.setProduct(p);
//            pi.setUrl(url);
//            pi.setPublicId(pid);
//            pi.setSortOrder(nextOrder++);
//            productImageRepo.save(pi);
//        }
//    }
//
//    @Override
//    public ProductForm getProductFormById(Long productId) {
//        Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
//        ProductForm form = new ProductForm();
//        form.setId(product.getId());
//        form.setName(product.getName());
//        form.setDescription(product.getDescription());
//        form.setStatus(product.getStatus());
//        form.setShopId(product.getShop().getId());
//        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
//
//        List<VariantForm> variantForms = variantRepo.findByProduct_IdOrderByPriceAsc(productId).stream()
//                .map(pv -> new VariantForm(pv.getId(), pv.getSize(), pv.getPrice(), pv.getVolumeMl()))
//                .collect(Collectors.toList());
//        form.setVariants(variantForms);
//        return form;
//    }
//
//    @Override
//    public List<ProductImage> getImagesByProductId(Long productId) {
//        return imageRepo.findByProduct_Id(productId);
//    }
//
//    @Override
//    @Transactional
//    public void createProductWithVariants(ProductForm form, List<MultipartFile> images) {
//        Shop shop = shopRepo.findById(form.getShopId()).orElseThrow(() -> new RuntimeException("Shop không hợp lệ"));
//        ProductCategory category = (form.getCategoryId() != null) ? categoryRepo.findById(form.getCategoryId()).orElse(null) : null;
//
//        String cleanStatus = "AVAILABLE"; // Default to AVAILABLE
//        if (StringUtils.hasText(form.getStatus())) { // Check if form status is not null or blank
//            cleanStatus = form.getStatus().trim(); // Trim whitespace just in case
//            // Remove trailing comma IF it exists (defensive check)
//            if (cleanStatus.endsWith(",")) {
//                cleanStatus = cleanStatus.substring(0, cleanStatus.length() - 1);
//            }
//        }
//        Product product = Product.builder()
//                .name(form.getName())
//                .description(form.getDescription())
//                .status(cleanStatus)
//                .shop(shop)
//                .category(category)
//                .build();
//
//        if (form.getVariants() != null && !form.getVariants().isEmpty()) {
//            product.setBasePrice(form.getVariants().get(0).getPrice());
//        } else {
//            product.setBasePrice(BigDecimal.ZERO);
//        }
//
//        Product savedProduct = productRepo.save(product);
//
//        List<ProductVariant> variants = form.getVariants().stream()
//                .filter(v -> v.getPrice() != null && v.getPrice().compareTo(BigDecimal.ZERO) > 0)
//                .map(v -> ProductVariant.builder()
//                        .product(savedProduct)
//                        .size(v.getSize())
//                        .price(v.getPrice())
//                        .volumeMl(v.getVolumeMl())
//                        .build())
//                .collect(Collectors.toList());
//        variantRepo.saveAll(variants);
//
//        if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
//            uploadAndSaveImages(savedProduct, images);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void updateProductWithVariants(ProductForm form, List<MultipartFile> images) {
//        Product product = productRepo.findById(form.getId()).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
//
//        Shop shop = shopRepo.findById(form.getShopId()).orElseThrow(() -> new RuntimeException("Shop không hợp lệ"));
//        ProductCategory category = (form.getCategoryId() != null) ? categoryRepo.findById(form.getCategoryId()).orElse(null) : null;
//
//        String cleanStatus = "AVAILABLE"; // Default
//        if (StringUtils.hasText(form.getStatus())) {
//            cleanStatus = form.getStatus().trim();
//            if (cleanStatus.endsWith(",")) {
//                cleanStatus = cleanStatus.substring(0, cleanStatus.length() - 1);
//            }
//        }
//        product.setName(form.getName());
//        product.setDescription(form.getDescription());
//        product.setStatus(cleanStatus);
//        product.setShop(shop);
//        product.setCategory(category);
//        if (form.getVariants() != null && !form.getVariants().isEmpty()) {
//            product.setBasePrice(form.getVariants().get(0).getPrice());
//        }
//
//        product.getVariants().clear();
//        variantRepo.flush(); // Xóa các variant cũ khỏi DB
//
//        for (VariantForm vForm : form.getVariants()) {
//            if (vForm.getPrice() != null && vForm.getPrice().compareTo(BigDecimal.ZERO) > 0) {
//                product.getVariants().add(ProductVariant.builder()
//                        .product(product).size(vForm.getSize()).price(vForm.getPrice()).volumeMl(vForm.getVolumeMl()).build());
//            }
//        }
//        productRepo.save(product);
//
//        if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
//            // Khi cập nhật, xóa ảnh cũ trước rồi tải ảnh mới
//            deleteAllImagesForProduct(product);
//            uploadAndSaveImages(product, images);
//        }
//    }
//
//    private void uploadAndSaveImages(Product product, List<MultipartFile> files) {
//        int sortOrder = 1;
//        for (MultipartFile file : files) {
//            if (file.isEmpty()) continue;
//            String folder = "products/" + product.getId();
//            String publicId = "prod_" + product.getId() + "_img_" + System.currentTimeMillis() + "_" + sortOrder;
//            try {
//                Map<String, Object> result = cloudinaryService.upload(file, folder, publicId);
//                ProductImage image = ProductImage.builder()
//                        .product(product)
//                        .url((String) result.get("secure_url"))
//                        .publicId((String) result.get("public_id"))
//                        .sortOrder(sortOrder++)
//                        .build();
//                imageRepo.save(image);
//            } catch (IOException e) {
//                throw new RuntimeException("Tải ảnh thất bại cho sản phẩm ID=" + product.getId(), e);
//            }
//        }
//    }
//
//    private void deleteAllImagesForProduct(Product product) {
//        List<ProductImage> imagesToDelete = new ArrayList<>(product.getImages());
//        for (ProductImage image : imagesToDelete) {
//            try {
//                if (image.getPublicId() != null) {
//                    cloudinaryService.delete(image.getPublicId());
//                }
//            } catch (IOException e) {
//                // Log lỗi nhưng không dừng lại
//                System.err.println("Lỗi xóa ảnh trên Cloudinary: " + image.getPublicId());
//            }
//        }
//        product.getImages().clear();
//        imageRepo.deleteAll(imagesToDelete);
//    }
//
//}

package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.admin.controller.AdminCatalogController.ProductForm;
import net.codejava.utea.admin.controller.AdminCatalogController.VariantForm;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.entity.ProductImage;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.repository.ProductImageRepository;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.catalog.service.AdminProductService;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.media.service.CloudinaryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepo;
    private final ProductCategoryRepository categoryRepo; // nếu chưa có, tạo JpaRepository<ProductCategory, Long>

    private final ShopRepository shopRepo;

    private final ProductImageRepository productImageRepo;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository variantRepo;
    private final ProductImageRepository imageRepo;

    @Override
    @Transactional
    public Page<Product> search(String q, Long shopId, String status, Pageable pageable) {
        String kw = (q == null || q.isBlank()) ? null : q.trim();
        String st = (status == null || status.isBlank()) ? "ALL" : status.trim();

        Page<Product> page = productRepo.adminSearch(kw, shopId, st, pageable);

        // Ép initialize trước khi trả ra view (vẫn còn trong @Transactional)
        page.getContent().forEach(p -> {
            // shop name (tránh lazy khi th:text="${p.shop.name}")
            if (p.getShop() != null) {
                // chạm getter để init
                p.getShop().getName();
            }
            // images (tránh lazy khi gọi getMainImageUrl())
            if (p.getImages() != null) {
                p.getImages().size();
            }
        });

        return page;
    }

    @Override
    @Transactional
    public void hide(Long id, String reason) {
        productRepo.findById(id).ifPresent(p -> {
            p.setStatus("HIDDEN");
            // TODO: nếu muốn lưu lý do, tạo bảng log; ở đây chỉ set status
        });
    }

    @Override
    @Transactional
    public void unhide(Long id) {
        productRepo.findById(id).ifPresent(p -> p.setStatus("AVAILABLE"));
        // hoặc productRepo.updateStatus(id, "AVAILABLE");
    }

    @Override
    @Transactional
    public void hardDelete(Long id) {
        productRepo.findById(id).ifPresent(p -> {
            if (p.getImages() != null) {
                p.getImages().forEach(img -> {
                    try {
                        String pid = img.getPublicId();
                        if (pid != null && !pid.isBlank()) {
                            cloudinaryService.delete(pid);
                        }
                    } catch (Exception ex) {
                        // log warn nhưng KHÔNG để văng 500
                        // log.warn("Cloudinary delete fail: {}", img.getPublicId(), ex);
                    }
                });
            }
            productRepo.delete(p); // cascade ALL + orphanRemoval dọn product_images
        });
    }

    @Override
    @Transactional
    public void updateBasic(Long id, String name, String description, BigDecimal basePrice, Long categoryId) {
        productRepo.findById(id).ifPresent(p -> {
            if (name != null && !name.isBlank())
                p.setName(name.trim());
            if (description != null)
                p.setDescription(description.trim());
            if (basePrice != null)
                p.setBasePrice(basePrice);
            if (categoryId != null) {
                ProductCategory cat = categoryRepo.findById(categoryId).orElse(null);
                p.setCategory(cat);
            }
        });
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepo.findById(id);
    }

    @Override
    public void create(String name, String description, BigDecimal basePrice, Long categoryId, Long shopId,
                       String status, MultipartFile[] images) {
        var shop = shopRepo.findById(shopId).orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
        var category = (categoryId == null) ? null : categoryRepo.findById(categoryId).orElse(null);

        Product p = new Product();
        p.setName(name.trim());
        p.setDescription(description);
        p.setBasePrice(basePrice);
        p.setStatus(status);
        p.setShop(shop);
        p.setCategory(category);
        p = productRepo.save(p); // cần id để gắn ảnh

        saveImages(p, images);
    }

    @Transactional
    @Override
    public void update(Long id, String name, String description, BigDecimal basePrice, Long categoryId, Long shopId,
                       String status, MultipartFile[] newImages) {
        var p = productRepo.findById(id).orElseThrow();
        var shop = shopRepo.findById(shopId).orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
        var category = (categoryId == null) ? null : categoryRepo.findById(categoryId).orElse(null);

        p.setName(name.trim());
        p.setDescription(description);
        p.setBasePrice(basePrice);
        p.setStatus(status);
        p.setShop(shop);
        p.setCategory(category);

        // Nếu có ảnh mới, thêm tiếp (không bắt buộc xoá ảnh cũ)
        if (newImages != null) {
            saveImages(p, newImages);
        }
    }

    private static String slugify(String input) {
        if (input == null) return "image";
        String n = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // bỏ dấu
        n = n.replaceAll("[^\\p{Alnum}]+", "-").replaceAll("(^-|-$)", "").toLowerCase();
        return n.isBlank() ? "image" : n;
    }

    @SuppressWarnings("unchecked")
    private void saveImages(Product p, MultipartFile[] images) {
        if (images == null) return;

        int nextOrder = productImageRepo.findMaxSortOrderByProductId(p.getId()).orElse(0) + 1;
        String nameSlug = slugify(p.getName());
        String folder = "products/" + p.getId();    // → sẽ thành utea/products/{id}

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;

            String ts = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String publicId = nameSlug + "_" + ts + "_" + nextOrder;  // ví dụ: tra-sua-matcha_20251025...

            Map<String, Object> res;
            try {
                res = cloudinaryService.upload(file, folder, publicId);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Upload ảnh thất bại cho sản phẩm ID=" + p.getId(), e);
            }

            String url = res.get("secure_url") != null
                    ? res.get("secure_url").toString()
                    : (res.get("url") != null ? res.get("url").toString() : null);
            String pid = res.get("public_id") != null ? res.get("public_id").toString() : publicId;

            ProductImage pi = new ProductImage();
            pi.setProduct(p);
            pi.setUrl(url);
            pi.setPublicId(pid);
            pi.setSortOrder(nextOrder++);
            productImageRepo.save(pi);
        }
    }

    @Override
    public ProductForm getProductFormById(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setStatus(product.getStatus());
        form.setShopId(product.getShop().getId());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);

        List<VariantForm> variantForms = variantRepo.findByProduct_IdOrderByPriceAsc(productId).stream()
                .map(pv -> new VariantForm(pv.getId(), pv.getSize(), pv.getPrice(), pv.getVolumeMl()))
                .collect(Collectors.toList());
        form.setVariants(variantForms);
        return form;
    }

    @Override
    public List<ProductImage> getImagesByProductId(Long productId) {
        return imageRepo.findByProduct_Id(productId);
    }

    @Override
    @Transactional
    public void createProductWithVariants(ProductForm form, List<MultipartFile> images) {
        Shop shop = shopRepo.findById(form.getShopId()).orElseThrow(() -> new RuntimeException("Shop không hợp lệ"));
        ProductCategory category = (form.getCategoryId() != null) ? categoryRepo.findById(form.getCategoryId()).orElse(null) : null;

        String cleanStatus = "AVAILABLE"; // Default to AVAILABLE
        if (StringUtils.hasText(form.getStatus())) { // Check if form status is not null or blank
            cleanStatus = form.getStatus().trim(); // Trim whitespace just in case
            // Remove trailing comma IF it exists (defensive check)
            if (cleanStatus.endsWith(",")) {
                cleanStatus = cleanStatus.substring(0, cleanStatus.length() - 1);
            }
        }
        Product product = Product.builder()
                .name(form.getName())
                .description(form.getDescription())
                .status(cleanStatus)
                .shop(shop)
                .category(category)
                .build();

        if (form.getVariants() != null && !form.getVariants().isEmpty()) {
            product.setBasePrice(form.getVariants().get(0).getPrice());
        } else {
            product.setBasePrice(BigDecimal.ZERO);
        }

        Product savedProduct = productRepo.save(product);

        List<ProductVariant> variants = form.getVariants().stream()
                .filter(v -> v.getPrice() != null && v.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .map(v -> ProductVariant.builder()
                        .product(savedProduct)
                        .size(v.getSize())
                        .price(v.getPrice())
                        .volumeMl(v.getVolumeMl())
                        .build())
                .collect(Collectors.toList());
        variantRepo.saveAll(variants);

        if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
            uploadAndSaveImages(savedProduct, images);
        }
    }

    @Override
    @Transactional
    public void updateProductWithVariants(ProductForm form, List<MultipartFile> images) {
        Product product = productRepo.findById(form.getId()).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Shop shop = shopRepo.findById(form.getShopId()).orElseThrow(() -> new RuntimeException("Shop không hợp lệ"));
        ProductCategory category = (form.getCategoryId() != null) ? categoryRepo.findById(form.getCategoryId()).orElse(null) : null;

        String cleanStatus = "AVAILABLE"; // Default
        if (StringUtils.hasText(form.getStatus())) {
            cleanStatus = form.getStatus().trim();
            if (cleanStatus.endsWith(",")) {
                cleanStatus = cleanStatus.substring(0, cleanStatus.length() - 1);
            }
        }
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setStatus(cleanStatus);
        product.setShop(shop);
        product.setCategory(category);
        if (form.getVariants() != null && !form.getVariants().isEmpty()) {
            product.setBasePrice(form.getVariants().get(0).getPrice());
        }

        product.getVariants().clear();
        variantRepo.flush(); // Xóa các variant cũ khỏi DB

        for (VariantForm vForm : form.getVariants()) {
            if (vForm.getPrice() != null && vForm.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                product.getVariants().add(ProductVariant.builder()
                        .product(product).size(vForm.getSize()).price(vForm.getPrice()).volumeMl(vForm.getVolumeMl()).build());
            }
        }
        productRepo.save(product);

        if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
            // Khi cập nhật, xóa ảnh cũ trước rồi tải ảnh mới
            deleteAllImagesForProduct(product);
            uploadAndSaveImages(product, images);
        }
    }

    private void uploadAndSaveImages(Product product, List<MultipartFile> files) {
        int sortOrder = 1;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String folder = "products/" + product.getId();
            String publicId = "prod_" + product.getId() + "_img_" + System.currentTimeMillis() + "_" + sortOrder;
            try {
                Map<String, Object> result = cloudinaryService.upload(file, folder, publicId);
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .url((String) result.get("secure_url"))
                        .publicId((String) result.get("public_id"))
                        .sortOrder(sortOrder++)
                        .build();
                imageRepo.save(image);
            } catch (IOException e) {
                throw new RuntimeException("Tải ảnh thất bại cho sản phẩm ID=" + product.getId(), e);
            }
        }
    }

    private void deleteAllImagesForProduct(Product product) {
        List<ProductImage> imagesToDelete = new ArrayList<>(product.getImages());
        for (ProductImage image : imagesToDelete) {
            try {
                if (image.getPublicId() != null) {
                    cloudinaryService.delete(image.getPublicId());
                }
            } catch (IOException e) {
                // Log lỗi nhưng không dừng lại
                System.err.println("Lỗi xóa ảnh trên Cloudinary: " + image.getPublicId());
            }
        }
        product.getImages().clear();
        imageRepo.deleteAll(imagesToDelete);
    }

}
