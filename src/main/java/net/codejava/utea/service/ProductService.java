package net.codejava.utea.service;

import net.codejava.utea.entity.Product;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<Product> getAllAvailableProducts();
    List<Product> getProductsByCategory(Long categoryId);
    Page<Product> getAllAvailableProductsPaged(Pageable pageable);
    Page<Product> getProductsByCategoryPaged(Long categoryId, Pageable pageable);
}
