package net.codejava.utea.service.impl;

import net.codejava.utea.entity.Product;
import net.codejava.utea.repository.ProductRepository;
import net.codejava.utea.customer.repository.projection.BestSellerRow;
import net.codejava.utea.customer.repository.OrderItemRepository;
import net.codejava.utea.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    private OrderItemRepository orderItemRepo;

    public ProductServiceImpl(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Autowired(required = false)
    public void setOrderItemRepo(OrderItemRepository orderItemRepo) {
        this.orderItemRepo = orderItemRepo;
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

    @Override
    public Optional<Product> findAvailableById(Long id) {
        return productRepo.findById(id).filter(p -> "AVAILABLE".equals(p.getStatus()));
    }

    @Override
    public List<Product> getByCategoryName(String categoryName) {
        return productRepo.findByCategory_CategoryNameAndStatus(categoryName, "AVAILABLE");
    }

    @Override
    public List<Product> getTop6BestSeller() {
        return productRepo.findTop6ByStatusOrderBySoldCountDesc("AVAILABLE");
    }

    @Override
    public List<Product> getTop6BestSellerFromOrders() {
        if (orderItemRepo == null) {
            return getTop6BestSeller();
        }
        var rows = orderItemRepo.topBestSellers(
                List.of("PAID", "COMPLETED"),
                PageRequest.of(0, 6)
        );
        return rows.stream().map(BestSellerRow::getProduct).toList();
    }
}
