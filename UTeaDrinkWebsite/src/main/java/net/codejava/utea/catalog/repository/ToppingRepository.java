package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    // Truy vấn topping theo Shop ID
    List<Topping> findByShopId(Long shopId);
}
