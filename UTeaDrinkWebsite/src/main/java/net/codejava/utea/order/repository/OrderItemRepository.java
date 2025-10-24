package net.codejava.utea.order.repository;

import net.codejava.utea.customer.repository.projection.BestSellerRow;
import net.codejava.utea.order.entity.OrderItem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
    List<BestSellerRow> topBestSellers(@Param("statuses") List<String> statuses,
                                       Pageable pageable);
}
