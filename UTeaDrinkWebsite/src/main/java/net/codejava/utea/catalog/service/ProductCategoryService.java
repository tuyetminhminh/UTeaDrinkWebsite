package net.codejava.utea.catalog.service;

import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.admin.controller.AdminCategoryController.CategoryForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryService {

    ProductCategory save(ProductCategory entity);

    List<ProductCategory> findAll();

    Page<ProductCategory> findAll(Pageable pageable);

    Optional<ProductCategory> findById(Long id);

    void deleteById(Long id);

    /**
     * Tìm kiếm theo tên (contains, ignore-case). Nếu name null/blank → trả về findAll(pageable).
     */
    Page<ProductCategory> search(String name, Pageable pageable);

    boolean nameExists(String name, Long excludeId);
    /**
     * Tạo mới một danh mục từ dữ liệu form.
     * @param form Dữ liệu từ người dùng.
     * @return Entity đã được lưu.
     */
    ProductCategory createCategory(CategoryForm form);

    /**
     * Cập nhật một danh mục từ dữ liệu form.
     * @param id ID của danh mục cần cập nhật.
     * @param form Dữ liệu mới từ người dùng.
     * @return Entity đã được cập nhật.
     */
    ProductCategory updateCategory(Long id, CategoryForm form);

    /**
     * Thay đổi trạng thái của danh mục (ACTIVE/INACTIVE).
     * @param id ID của danh mục.
     * @param newStatus Trạng thái mới.
     */
    void updateStatus(Long id, String newStatus);
}
