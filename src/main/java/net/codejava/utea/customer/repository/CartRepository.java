package net.codejava.utea.customer.repository;

import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.entity.Product;
import net.codejava.utea.customer.entity.ProductVariant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {
            "product",
            "variant",
            "variant.size"
    })
    List<Cart> findByCustomer(Customer customer);

    Optional<Cart> findByCustomerAndProduct_ProductIdAndVariantIsNull(Customer c, Long productId);
    void deleteByCustomerAndProduct_ProductIdAndVariantIsNull(Customer c, Long productId);

    Optional<Cart> findByCustomerAndVariant_Id(Customer c, Long variantId);
    void deleteByCustomerAndVariant_Id(Customer c, Long variantId);

    void deleteByCustomer(Customer customer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Cart c set c.isSelected = :selected where c.customer = :customer")
    int updateAllSelectedByCustomer(@Param("customer") Customer customer,@Param("selected") boolean selected);

    Optional<Cart> findByCartIdAndCustomer(Long cartId, Customer customer);

    Optional<Cart> findByCustomerAndProductAndVariant(Customer customer,Product product,ProductVariant variant);

    List<Cart> findByCustomerAndIsSelectedTrue(Customer customer);

    @Modifying @Transactional
    @Query("delete from Cart c where c.customer = :customer and c.isSelected = true")
    void deleteSelectedByCustomer(@Param("customer") Customer customer);
}

