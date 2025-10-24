package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    List<Promotion> findByStatus(String status);
    
    List<Promotion> findByScope(PromoScope scope);
    
    List<Promotion> findByShopIdAndStatus(Long shopId, String status);
}

