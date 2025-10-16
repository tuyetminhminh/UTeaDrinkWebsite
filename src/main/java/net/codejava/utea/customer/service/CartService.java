package net.codejava.utea.customer.service;

import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.entity.Customer;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    List<Cart> getCartByCustomer(Customer customer);

    // theo product (KHÔNG variant)
    void addToCart(Customer customer, Long productId, int quantity);
    void updateQuantity(Customer customer, Long productId, int quantity);
    void removeFromCart(Customer customer, Long productId);

    void addToCartWithVariant(Customer customer, Long productId, Long variantId, int quantity);
    void updateQuantityByVariant(Customer customer, Long variantId, int quantity);
    void removeFromCartByVariant(Customer customer, Long variantId);

    void clearCart(Customer customer);

    void setSelectedByProductNoVariant(Customer c, Long productId, boolean selected);
    void setSelectedByVariant(Customer c, Long variantId, boolean selected);
    int setSelectedAll(Customer c, boolean selected);

    void changeVariant(Customer customer, Long cartId, Long newVariantId);

    BigDecimal calculateSubtotalAll(Customer customer);
    BigDecimal calculateSubtotalSelected(Customer customer);
    List<Cart> getSelectedItems(Customer customer);
    BigDecimal getSelectedSubtotal(Customer customer);
    BigDecimal estimateShippingFee(BigDecimal subtotal);
}
