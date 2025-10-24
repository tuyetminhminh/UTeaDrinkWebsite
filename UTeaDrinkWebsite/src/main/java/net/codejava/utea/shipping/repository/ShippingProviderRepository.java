package net.codejava.utea.shipping.repository;

import net.codejava.utea.shipping.entity.ShippingProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {
    
    Optional<ShippingProvider> findByName(String name);
    
    List<ShippingProvider> findByStatus(String status);
}

