package net.codejava.utea.catalog.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    @Override
    public Page<Product> getProductsByCategoryPaged(Long categoryId, Pageable pageable) {
        return productRepo.findByCategory_IdAndStatus(categoryId, "AVAILABLE", pageable);
    }

    @Override
    public Page<Product> getAllAvailableProductsPaged(Pageable pageable) {
        return productRepo.findByStatus("AVAILABLE", pageable);
    }

    @Override
    public List<Product> getTop6BestSellerFromOrders() {
        return productRepo.findTop6ByStatusOrderBySoldCountDesc("AVAILABLE");
    }

    @Override
    public List<Product> getByCategoryName(String categoryName) {
        return productRepo.findByCategory_NameAndStatusOrderByCreatedAtDesc(categoryName, "AVAILABLE");
    }

    @Override
    public Optional<Product> findAvailableById(Long id) {
        return productRepo.findByIdAndStatus(id, "AVAILABLE");
    }

    @Override
    public List<Product> getAllAvailableProducts() {
        // nếu cần list không phân trang:
        return productRepo.findByStatus("AVAILABLE", Pageable.unpaged()).getContent();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepo.findByCategory_IdAndStatus(categoryId, "AVAILABLE", Pageable.unpaged()).getContent();
    }

    @Override
    public List<Product> getTop6BestSeller() {
        return getTop6BestSellerFromOrders();
    }

    @Override
    public Page<Product> search(String q, Long categoryId, BigDecimal min, BigDecimal max, String sort, Pageable pageable) {
        // sort hiện tại đang dựa vào Pageable truyền vào; nếu muốn sort theo 'sort' custom,
        // hãy build Pageable tương ứng trước khi gọi service (controller).
        String kw = (q == null || q.isBlank()) ? null : q.trim();
        return productRepo.search(kw, categoryId, min, max, pageable);
    }

    @Override
    public Page<Product> search(String kw, Long catId, BigDecimal min, BigDecimal max, Pageable pageable) {
        String key = (kw == null || kw.isBlank()) ? null : kw.trim();
        return productRepo.search(key, catId, min, max, pageable);
    }

}
