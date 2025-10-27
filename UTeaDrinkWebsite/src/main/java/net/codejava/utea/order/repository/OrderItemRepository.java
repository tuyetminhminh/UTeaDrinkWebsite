package net.codejava.utea.order.repository;

import net.codejava.utea.customer.repository.projection.BestSellerRow;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select oi.product as product,
               sum(oi.quantity) as total
        from OrderItem oi
        where oi.order.status in :statuses
        group by oi.product
        order by sum(oi.quantity) desc
    """)
    List<BestSellerRow> topBestSellers(@Param("statuses") Collection<OrderStatus> statuses,
                                       Pageable pageable);

    /**
     * Tính tổng số lượng đã bán của nhiều sản phẩm theo trạng thái đơn hàng (batch query)
     */
    @Query("""
        select oi.product.id, sum(oi.quantity)
        from OrderItem oi
        where oi.product.id in :productIds
        and oi.order.status = :status
        group by oi.product.id
    """)
    List<Object[]> sumQuantityByProductsAndStatus(@Param("productIds") Collection<Long> productIds,
                                                   @Param("status") OrderStatus status);

    @Query("""
        select oi.product as product,
               sum(oi.quantity) as total
        from OrderItem oi
        where oi.order.status in :statuses
          and oi.order.shop.id = :shopId
        group by oi.product
        order by sum(oi.quantity) desc
    """)
    List<BestSellerRow> topBestSellersByShop(@Param("shopId") Long shopId,
                                             @Param("statuses") Collection<OrderStatus> statuses,
                                             Pageable pageable);
}
