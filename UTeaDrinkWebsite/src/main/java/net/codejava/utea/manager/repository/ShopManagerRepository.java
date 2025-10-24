package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopManagerRepository extends JpaRepository<ShopManager, Long> {
    
    Optional<ShopManager> findByManagerId(Long managerId);
    
    Optional<ShopManager> findByShopId(Long shopId);
    
    boolean existsByManagerId(Long managerId);
}

