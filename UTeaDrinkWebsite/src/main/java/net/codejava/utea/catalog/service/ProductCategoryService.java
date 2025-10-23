package net.codejava.utea.catalog.service;

import net.codejava.utea.catalog.entity.ProductCategory;
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

    /**
     * Kiểm tra tên đã tồn tại (không phân biệt hoa thường).
     * @param name      tên cần kiểm tra
     * @param excludeId bỏ qua id (khi update), truyền null nếu tạo mới
     */
    boolean nameExists(String name, Long excludeId);
}
