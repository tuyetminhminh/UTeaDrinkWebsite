//package net.codejava.utea.order.repository;
//
//import net.codejava.utea.common.entity.User;
//import net.codejava.utea.order.entity.Order;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface OrderRepository extends JpaRepository<Order, Long> {
//    Optional<Order> findByOrderCode(String orderCode);
//    List<Order> findByUserOrderByCreatedAtDesc(User user);
//    List<Order> findByUser_Id(Long userId);
//    List<Order> findByShop_Id(Long shopId);
//
//    /**
//     * Đếm số đơn hàng của user (để kiểm tra first order)
//     */
//    long countByUser(User user);
//}

package net.codejava.utea.order.repository;

import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.dto.RecentOrderDTO;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;


public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUser_Id(Long userId);
    List<Order> findByShop_Id(Long shopId);

    /**
     * Đếm số đơn hàng của user (để kiểm tra first order)
     */
    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new net.codejava.utea.order.dto.RecentOrderDTO(o.orderCode, u.fullName, o.createdAt, o.total) FROM Order o JOIN o.user u ORDER BY o.createdAt DESC")
    List<RecentOrderDTO> findRecentOrders(Pageable pageable);

    //@Query("SELECT CAST(o.createdAt AS DATE), SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate GROUP BY CAST(o.createdAt AS DATE) ORDER BY CAST(o.createdAt AS DATE) ASC")
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :startDate")
    List<Order> findCompletedOrdersSince(
            @Param("startDate") LocalDateTime startDate,
            @Param("statuses") List<OrderStatus> statuses // Nhận vào một List<OrderStatus>
    );
}