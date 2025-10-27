package net.codejava.utea.catalog.service;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import net.codejava.utea.catalog.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllAvailableProducts();
    List<Product> getProductsByCategory(Long categoryId);
    Page<Product> getAllAvailableProductsPaged(Pageable pageable);
    Page<Product> getProductsByCategoryPaged(Long categoryId, Pageable pageable);

    Optional<Product> findAvailableById(Long id);
    List<Product> getByCategoryName(String categoryName);

    List<Product> getTop6BestSeller();
    List<Product> getTop6BestSellerFromOrders();
    
    Page<Product> search(String q,
            Long categoryId,
            BigDecimal min,
            BigDecimal max,
            String sort,
            Pageable pageable);
	Page<Product> search(String kw, Long catId, BigDecimal min, BigDecimal max, Pageable pageable);
}
