package net.codejava.utea.engagement.service;

import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.engagement.entity.ViewedProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ViewedProductService {

    /**
     * Track khi user xem một sản phẩm
     * Nếu đã xem rồi thì update thời gian
     */
    void trackView(User user, Product product);

    /**
     * Track khi user xem một sản phẩm (by ID)
     */
    void trackView(Long userId, Long productId);

    /**
     * Lấy danh sách sản phẩm đã xem gần đây của user (phân trang)
     */
    Page<ViewedProduct> getRecentlyViewed(Long userId, Pageable pageable);

    /**
     * Lấy danh sách sản phẩm đã xem gần đây của user (List - limit)
     */
    List<Product> getRecentlyViewedProducts(Long userId, int limit);

    /**
     * Đếm số sản phẩm đã xem của user
     */
    long countViewed(Long userId);

    /**
     * Xóa lịch sử xem của user
     */
    void clearHistory(Long userId);
}

