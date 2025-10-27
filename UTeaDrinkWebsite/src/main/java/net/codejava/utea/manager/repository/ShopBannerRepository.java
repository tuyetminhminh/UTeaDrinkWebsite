package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Sort;

@Repository
public interface ShopBannerRepository extends JpaRepository<ShopBanner, Long> {

    List<ShopBanner> findByShopIdOrderBySortOrderAsc(Long shopId);

    List<ShopBanner> findByShopIdAndActiveOrderBySortOrderAsc(Long shopId, boolean active);

    List<ShopBanner> findByActiveTrue(Sort sort);
}

