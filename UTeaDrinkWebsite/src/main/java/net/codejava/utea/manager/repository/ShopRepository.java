package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    Optional<Shop> findByName(String name);
    
    List<Shop> findByStatus(String status);
}

