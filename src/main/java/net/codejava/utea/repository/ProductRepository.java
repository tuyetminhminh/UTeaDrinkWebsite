package net.codejava.utea.repository;

import net.codejava.utea.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(String status);
    List<Product> findByCategory_CategoryId(Long categoryId);
    Page<Product> findByStatus(String status, Pageable pageable);
    Page<Product> findByCategory_CategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

}
