package net.codejava.utea.order.repository;

import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUser_Id(Long userId);
    List<Order> findByShop_Id(Long shopId);
    
    /**
     * Đếm số đơn hàng của user (để kiểm tra first order)
     */
    long countByUser(User user);
}