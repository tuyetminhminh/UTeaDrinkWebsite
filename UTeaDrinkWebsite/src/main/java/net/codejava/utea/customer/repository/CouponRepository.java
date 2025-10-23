package net.codejava.utea.customer.repository;

import net.codejava.utea.customer.entity.Coupon;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("""
      select c from Coupon c
      where c.code = :code
        and c.active = true
        and (c.startDate is null or c.startDate <= CURRENT_TIMESTAMP)
        and (c.endDate   is null or c.endDate   >= CURRENT_TIMESTAMP)
    """)
    Optional<Coupon> findByCodeActiveNow(@Param("code") String code);

    @Query("""
      select c from Coupon c
      where c.active = true
        and (c.startDate is null or c.startDate <= CURRENT_TIMESTAMP)
        and (c.endDate   is null or c.endDate   >= CURRENT_TIMESTAMP)
      order by c.createdAt desc
    """)
    List<Coupon> findAllActiveNow();
}
