package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopSectionRepository extends JpaRepository<ShopSection, Long> {
    
    List<ShopSection> findByShopIdOrderBySortOrderAsc(Long shopId);
    
    List<ShopSection> findByShopIdAndIsActiveOrderBySortOrderAsc(Long shopId, boolean isActive);
}

