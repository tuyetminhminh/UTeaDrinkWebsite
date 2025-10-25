package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.admin.controller.AdminCategoryController.CategoryForm;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.service.ProductCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repo;

    @Override
    public ProductCategory save(ProductCategory entity) {
        return repo.save(entity);
    }

    @Override
    public List<ProductCategory> findAll() {
        return repo.findAll();
    }

    @Override
    public Page<ProductCategory> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Page<ProductCategory> search(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Override
    public boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank())
            return false;
        String n = name.trim();
        return (excludeId == null) ? repo.existsByNameIgnoreCase(n) : repo.existsByNameIgnoreCaseAndIdNot(n, excludeId);
    }
    @Override
    public ProductCategory createCategory(CategoryForm form) {
        ProductCategory c = new ProductCategory();
        c.setName(form.getName().trim());
        c.setDescription(form.getDescription());
        // LOGIC NGHIỆP VỤ ĐÃ Ở ĐÂY
        c.setStatus(Objects.equals(form.getActive(), true) ? "ACTIVE" : "INACTIVE");
        return repo.save(c);
    }

    @Override
    public ProductCategory updateCategory(Long id, CategoryForm form) {
        ProductCategory c = repo.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        c.setName(form.getName().trim());
        c.setDescription(form.getDescription());
        // LOGIC NGHIỆP VỤ ĐÃ Ở ĐÂY
        c.setStatus(Objects.equals(form.getActive(), true) ? "ACTIVE" : "INACTIVE");
        return repo.save(c);
    }

    @Override
    public void updateStatus(Long id, String newStatus) {
        ProductCategory category = repo.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        category.setStatus(newStatus);
        repo.save(category);
    }
}
