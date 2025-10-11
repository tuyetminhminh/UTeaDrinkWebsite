package net.codejava.utea.service.impl;

import net.codejava.utea.entity.Product;
import net.codejava.utea.repository.ProductRepository;
import net.codejava.utea.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    public ProductServiceImpl(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public List<Product> getAllAvailableProducts() {
        return productRepo.findByStatus("AVAILABLE");
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepo.findByCategory_CategoryId(categoryId);
    }
    @Override
    public Page<Product> getAllAvailableProductsPaged(Pageable pageable) {
        return productRepo.findByStatus("AVAILABLE", pageable);
    }

    @Override
    public Page<Product> getProductsByCategoryPaged(Long categoryId, Pageable pageable) {
        return productRepo.findByCategory_CategoryIdAndStatus(categoryId, "AVAILABLE", pageable);
    }

}
