package net.codejava.utea.service;

import net.codejava.utea.entity.Product;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

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
}
