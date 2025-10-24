package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopBannerRepository extends JpaRepository<ShopBanner, Long> {
    
    List<ShopBanner> findByShopIdOrderBySortOrderAsc(Long shopId);
    
    List<ShopBanner> findByShopIdAndIsActiveOrderBySortOrderAsc(Long shopId, boolean isActive);
}

