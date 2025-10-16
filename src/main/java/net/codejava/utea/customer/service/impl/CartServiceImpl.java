package net.codejava.utea.customer.service.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.customer.repository.CartRepository;
import net.codejava.utea.repository.ProductRepository;
import net.codejava.utea.customer.repository.ProductVariantRepository;
import net.codejava.utea.customer.service.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service @RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;

    @Override
    public List<Cart> getCartByCustomer(Customer customer) {
        return cartRepo.findByCustomer(customer);
    }

    @Override @Transactional
    public void addToCart(Customer customer, Long productId, int quantity) {
        var product = productRepo.findById(productId).orElseThrow();
        var existing = cartRepo.findByCustomerAndProduct_ProductIdAndVariantIsNull(customer, productId);

        if (existing.isPresent()) {
            var c = existing.get();
            c.setQuantity(Math.max(1, c.getQuantity() + quantity));
            c.setUnitPrice(product.getPrice());
        } else {
            cartRepo.save(Cart.builder()
                    .customer(customer)
                    .product(product)
                    .variant(null)
                    .unitPrice(product.getPrice())
                    .quantity(Math.max(1, quantity))
                    .isSelected(true)
                    .build());
        }
    }

    @Override @Transactional
    public void updateQuantity(Customer customer, Long productId, int quantity) {
        var item = cartRepo.findByCustomerAndProduct_ProductIdAndVariantIsNull(customer, productId).orElseThrow();
        item.setQuantity(Math.max(1, quantity));
    }

    @Override @Transactional
    public void removeFromCart(Customer customer, Long productId) {
        cartRepo.deleteByCustomerAndProduct_ProductIdAndVariantIsNull(customer, productId);
    }

    @Override @Transactional
    public void addToCartWithVariant(Customer customer, Long productId, Long variantId, int quantity) {
        var product = productRepo.findById(productId).orElseThrow();
        var variant = variantRepo.findById(variantId).orElseThrow();

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Variant không thuộc sản phẩm");
        }

        var existing = cartRepo.findByCustomerAndVariant_Id(customer, variantId);
        if (existing.isPresent()) {
            var c = existing.get();
            c.setQuantity(Math.max(1, c.getQuantity() + quantity));
            c.setUnitPrice(variant.getPrice());
        } else {
            cartRepo.save(Cart.builder()
                    .customer(customer)
                    .product(product)
                    .variant(variant)
                    .unitPrice(variant.getPrice())
                    .quantity(Math.max(1, quantity))
                    .isSelected(true)
                    .build());
        }
    }

    @Override @Transactional
    public void updateQuantityByVariant(Customer customer, Long variantId, int quantity) {
        var item = cartRepo.findByCustomerAndVariant_Id(customer, variantId).orElseThrow();
        item.setQuantity(Math.max(1, quantity));
    }

    @Override @Transactional
    public void removeFromCartByVariant(Customer customer, Long variantId) {
        cartRepo.deleteByCustomerAndVariant_Id(customer, variantId);
    }

    @Override @Transactional
    public int setSelectedAll(Customer c, boolean selected) {
        return cartRepo.updateAllSelectedByCustomer(c, selected);
    }

    @Override @Transactional
    public void clearCart(Customer customer) {
        cartRepo.deleteByCustomer(customer);
    }

    @Override @Transactional
    public void setSelectedByProductNoVariant(Customer c, Long productId, boolean selected) {
        var item = cartRepo.findByCustomerAndProduct_ProductIdAndVariantIsNull(c, productId).orElseThrow();
        item.setSelected(selected);
    }

    @Override @Transactional
    public void setSelectedByVariant(Customer c, Long variantId, boolean selected) {
        var item = cartRepo.findByCustomerAndVariant_Id(c, variantId).orElseThrow();
        item.setSelected(selected);
    }

    private BigDecimal sum(List<Cart> items) {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateSubtotalAll(Customer customer) {
        return sum(getCartByCustomer(customer));
    }

    @Override
    public BigDecimal calculateSubtotalSelected(Customer customer) {
        return sum(getCartByCustomer(customer).stream().filter(Cart::isSelected).toList());
    }

    @Override
    public List<Cart> getSelectedItems(Customer customer) {
        return cartRepo.findByCustomer(customer).stream().filter(Cart::isSelected).toList();
    }
    @Override
    @Transactional
    public void changeVariant(Customer c, Long cartId, Long newVariantId) {
        var item = cartRepo.findByCartIdAndCustomer(cartId, c)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        var newVariant = variantRepo.findById(newVariantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (item.getVariant() != null && newVariantId.equals(item.getVariant().getId())) {
            return;
        }

        if (!item.getProduct().getProductId().equals(newVariant.getProduct().getProductId())) {
            throw new IllegalArgumentException("Variant không thuộc cùng sản phẩm");
        }

        var existingOpt = cartRepo.findByCustomerAndProductAndVariant(c, item.getProduct(), newVariant);
        if (existingOpt.isPresent() && !existingOpt.get().getCartId().equals(item.getCartId())) {
            var existed = existingOpt.get();
            existed.setQuantity(existed.getQuantity() + item.getQuantity());
            cartRepo.delete(item);
            return;
        }

        item.setVariant(newVariant);
        item.setUnitPrice(newVariant.getPrice());
        cartRepo.save(item);
    }
    @Override
    public BigDecimal getSelectedSubtotal(Customer customer) {
        return calculateSubtotalSelected(customer);
    }

    @Override
    public BigDecimal estimateShippingFee(BigDecimal subtotal) {
        return (subtotal.compareTo(new BigDecimal("500000")) >= 0)
                ? BigDecimal.ZERO
                : new BigDecimal("15000");
    }
}
