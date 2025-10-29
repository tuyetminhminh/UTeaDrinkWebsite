package net.codejava.utea.customer.repository;

import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // NẠP SẴN product + product.images + variant để dùng ở view
    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    List<CartItem> findByCart(Cart cart);

    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    List<CartItem> findByCartAndSelectedTrue(Cart cart);

    Optional<CartItem> findByCartIdAndId(Long cartId, Long itemId);

    Optional<CartItem> findByCartIdAndVariant(Long cartId, ProductVariant variant);

    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    Optional<CartItem> findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJson(Long cartId, Long productId, String toppingsJson);

    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    Optional<CartItem> findByCartIdAndProductIdAndVariant_IdAndToppingsJson(Long cartId, Long productId, Long variantId, String toppingsJson);

    // Thêm phương thức tìm theo product khi không có topping (match format với phương thức đã có)
    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    Optional<CartItem> findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJsonIsNull(Long cartId, Long productId);
    
    @EntityGraph(attributePaths = {"product", "product.images", "variant"})
    Optional<CartItem> findByCartIdAndProductIdAndVariant_IdAndToppingsJsonIsNull(Long cartId, Long productId, Long variantId);

}
