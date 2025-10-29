package net.codejava.utea.catalog.service;

import net.codejava.utea.admin.controller.AdminCatalogController.ProductForm;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductImage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AdminProductService {

    Optional<Product> findById(Long id);

    Page<Product> search(String q, Long shopId, String status, Pageable pageable);

    void hide(Long id, String reason);

    void unhide(Long id);

    void hardDelete(Long id);

    void updateBasic(Long id, String name, String description, BigDecimal basePrice, Long categoryId);

    void create(String name, String description, BigDecimal basePrice, Long categoryId, Long shopId, String status,
                MultipartFile[] images);

    void update(Long id, String name, String description, BigDecimal basePrice, Long categoryId, Long shopId,
                String status, MultipartFile[] newImages);

    // Lấy ProductForm cho trang Edit
    ProductForm getProductFormById(Long productId);

    // Lấy ảnh
    List<ProductImage> getImagesByProductId(Long productId);

    // Tạo sản phẩm
    void createProductWithVariants(ProductForm form, List<MultipartFile> images);

    // Cập nhật sản phẩm
    void updateProductWithVariants(ProductForm form, List<MultipartFile> images);
}
