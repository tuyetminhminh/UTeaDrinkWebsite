package net.codejava.utea.customer.repository;

import net.codejava.utea.common.entity.User;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.customer.entity.Cart;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
   
    Optional<Cart> findByUser(User user);
}

