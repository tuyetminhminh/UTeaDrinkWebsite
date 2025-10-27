package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("""
           select p from Promotion p
           where p.status = 'ACTIVE'
             and (p.activeFrom is null or p.activeFrom <= :now)
             and (p.activeTo   is null or p.activeTo   >= :now)
           """)
    List<Promotion> findActiveNow(LocalDateTime now);

    // ✅ XOÁ @Query — để JPA tự sinh
    List<Promotion> findByShop_IdAndStatus(Long shopId, String status);

    // ✅ Thêm cho DataInitializer
    List<Promotion> findByScope(PromoScope scope);
}
