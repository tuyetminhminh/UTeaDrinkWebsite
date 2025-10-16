package net.codejava.utea.customer.repository;

import net.codejava.utea.entity.Customer;
import net.codejava.utea.customer.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByCustomerOrderByCreatedAtDesc(Customer customer);
}