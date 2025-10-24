package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.repository.CartItemRepository;
import net.codejava.utea.customer.repository.CartRepository;
import net.codejava.utea.customer.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;

    // -----------------------------
    // 🧩 Utility Methods
    // -----------------------------
    @Override
    public Cart getOrCreate(User user) {
        return cartRepo.findByUser(user)
                .orElseGet(() -> {
                    Cart c = Cart.builder().user(user).build();
                    return cartRepo.save(c);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> listItems(User user) {
        return itemRepo.findByCart(getOrCreate(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> listSelected(User user) {
        return itemRepo.findByCartAndSelectedTrue(getOrCreate(user));
    }

    // -----------------------------
    // 🛒 Add / Update / Remove
    // -----------------------------
    @Override
    public CartItem addItem(User user, Long productId, Long variantId, int qty) {
        Cart cart = getOrCreate(user);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepo.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));
        }

        // Tìm item trùng product+variant để cộng dồn
        var existing = itemRepo.findByCartIdAndVariant(cart.getId(), variant);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + qty);
            return itemRepo.save(item);
        }

        // Nếu chưa có -> tạo mới
        BigDecimal unitPrice = (variant != null)
                ? variant.getPrice()
                : product.getBasePrice();

        CartItem item = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(qty)
                .unitPrice(unitPrice)
                .selected(true)
                .build();

        return itemRepo.save(item);
    }

    @Override
    public CartItem updateQty(User user, Long itemId, int qty) {
        Cart cart = getOrCreate(user);
        CartItem item = itemRepo.findByCartIdAndId(cart.getId(), itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
        if (qty <= 0) {
            itemRepo.delete(item);
            return null;
        }
        item.setQuantity(qty);
        return itemRepo.save(item);
    }

    @Override
    public void toggleSelect(User user, Long itemId, boolean selected) {
        Cart cart = getOrCreate(user);
        CartItem item = itemRepo.findByCartIdAndId(cart.getId(), itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item"));
        item.setSelected(selected);
        itemRepo.save(item);
    }

    @Override
    public void removeItem(User user, Long itemId) {
        Cart cart = getOrCreate(user);
        itemRepo.findByCartIdAndId(cart.getId(), itemId)
                .ifPresent(itemRepo::delete);
    }

    @Override
    public void clear(User user) {
        Cart cart = getOrCreate(user);
        var items = itemRepo.findByCart(cart);
        itemRepo.deleteAll(items);
    }

    // -----------------------------
    // 💰 Tính toán
    // -----------------------------
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSubtotal(User user) {
        return listItems(user).stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSelectedSubtotal(User user) {
        return listSelected(user).stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal estimateShippingFee(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;

        // Ví dụ: miễn phí ship trên 200k, còn lại tính 20k
        return subtotal.compareTo(BigDecimal.valueOf(200_000)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(20000);
    }
}
