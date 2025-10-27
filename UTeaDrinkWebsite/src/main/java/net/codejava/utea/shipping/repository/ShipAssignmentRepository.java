package net.codejava.utea.shipping.repository;

import net.codejava.utea.shipping.entity.ShipAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipAssignmentRepository extends JpaRepository<ShipAssignment, Long> {
    
    Optional<ShipAssignment> findByOrderId(Long orderId);
    
    List<ShipAssignment> findByShipperId(Long shipperId);
    
    List<ShipAssignment> findByShipperIdAndStatus(Long shipperId, String status);
}

