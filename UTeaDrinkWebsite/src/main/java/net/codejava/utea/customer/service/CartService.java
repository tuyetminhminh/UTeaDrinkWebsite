package net.codejava.utea.customer.service;

import net.codejava.utea.common.entity.User;
import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.customer.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    Cart getOrCreate(User user);

    List<CartItem> listItems(User user);

    List<CartItem> listSelected(User user);

    CartItem addItem(User user, Long productId, Long variantId, int qty);

    CartItem addItem(User user, Long productId, Long variantId, int qty, List<Long> toppingIds);

    CartItem updateToppings(User user, Long itemId, List<Long> toppingIds);

    CartItem updateQty(User user, Long itemId, int qty);

    void toggleSelect(User user, Long itemId, boolean selected);

    void removeItem(User user, Long itemId);

    void clear(User user);

    BigDecimal getSubtotal(User user);

    BigDecimal getSelectedSubtotal(User user);

    BigDecimal estimateShippingFee(BigDecimal subtotal);

    void clearSelected(User user);
}
