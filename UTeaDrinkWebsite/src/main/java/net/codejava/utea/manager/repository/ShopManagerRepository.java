package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopManagerRepository extends JpaRepository<ShopManager, Long> {
    
    // Sửa: manager là field name trong entity, manager.id để truy cập User ID
    Optional<ShopManager> findByManager_Id(Long managerId);
    
    Optional<ShopManager> findByShop_Id(Long shopId);
    
    boolean existsByManager_Id(Long managerId);
}

