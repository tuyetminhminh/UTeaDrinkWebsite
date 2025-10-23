package net.codejava.utea.customer.repository;

import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.customer.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	
	List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartAndSelectedTrue(Cart cart);

    Optional<CartItem> findByCartIdAndId(Long cartId, Long itemId);

    // để merge dòng cùng product+variant nếu cần
    Optional<CartItem> findByCartIdAndVariant(Long cartId, ProductVariant variant);
}