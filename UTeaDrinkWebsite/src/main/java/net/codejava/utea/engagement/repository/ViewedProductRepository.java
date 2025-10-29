package net.codejava.utea.engagement.repository;

import net.codejava.utea.engagement.entity.ViewedProduct;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Long> {

    /**
     * Tìm ViewedProduct theo user và product
     */
    Optional<ViewedProduct> findByUserAndProduct(User user, Product product);

    /**
     * Lấy danh sách sản phẩm đã xem của user, sắp xếp theo thời gian gần nhất
     * EntityGraph để load sẵn product và product.images
     */
    @EntityGraph(attributePaths = {"product", "product.images", "product.category"})
    Page<ViewedProduct> findByUser_IdOrderByLastSeenAtDesc(Long userId, Pageable pageable);

    /**
     * Lấy danh sách sản phẩm đã xem của user (List)
     */
    @EntityGraph(attributePaths = {"product", "product.images", "product.category"})
    List<ViewedProduct> findByUser_IdOrderByLastSeenAtDesc(Long userId);

    /**
     * Đếm số lượng sản phẩm đã xem của user
     */
    long countByUser_Id(Long userId);

    /**
     * Xóa tất cả viewed products của user
     */
    void deleteByUser_Id(Long userId);
}

