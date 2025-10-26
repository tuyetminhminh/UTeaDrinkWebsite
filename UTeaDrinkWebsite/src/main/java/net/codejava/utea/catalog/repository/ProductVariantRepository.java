package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Lấy toàn bộ biến thể của một sản phẩm, sắp xếp theo giá tăng dần
    List<ProductVariant> findByProduct_IdOrderByPriceAsc(Long productId);
}
