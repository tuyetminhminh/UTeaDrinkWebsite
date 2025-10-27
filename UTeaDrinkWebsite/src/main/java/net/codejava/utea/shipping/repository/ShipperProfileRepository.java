package net.codejava.utea.shipping.repository;

import net.codejava.utea.shipping.entity.ShipperProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipperProfileRepository extends JpaRepository<ShipperProfile, Long> {
    
    Optional<ShipperProfile> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}

