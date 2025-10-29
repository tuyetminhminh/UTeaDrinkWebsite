package net.codejava.utea.customer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.entity.Topping;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.catalog.repository.ToppingRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ToppingRepository toppingRepo;

    private final ObjectMapper om = new ObjectMapper();

    // =========================
    // Utils
    // =========================
    @Override
    public Cart getOrCreate(User user) {
        return cartRepo.findByUser(user).orElseGet(() -> cartRepo.save(Cart.builder().user(user).build()));
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

    // =========================
    // Add / Update / Remove
    // =========================
    @Override
    public CartItem addItem(User user, Long productId, Long variantId, int qty) {
        return addItem(user, productId, variantId, qty, null);
    }

    @Override
    public CartItem addItem(User user, Long productId, Long variantId, int qty, List<Long> toppingIds) {
        Cart cart = getOrCreate(user);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductVariant variant = (variantId != null)
                ? variantRepo.findById(variantId).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"))
                : null;

        BigDecimal base = (variant != null) ? variant.getPrice() : product.getBasePrice();
        BigDecimal topSum = sumToppingPrice(toppingIds);
        String topsJson = normalizeToppingsJson(toppingIds);
        BigDecimal unitPrice = base.add(topSum);

        // Gộp dòng theo product + variant + toppingsJson
        Optional<CartItem> existing;
        if (variant == null) {
            existing = (topsJson == null)
                    ? itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJsonIsNull(cart.getId(), productId)
                    : itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJson(cart.getId(), productId, topsJson);
        } else {
            existing = (topsJson == null)
                    ? itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJsonIsNull(cart.getId(), productId, variant.getId())
                    : itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJson(cart.getId(), productId, variant.getId(), topsJson);
        }

        if (existing.isPresent()) {
            CartItem it = existing.get();
            it.setQuantity(it.getQuantity() + qty);
            return itemRepo.save(it);
        }

        CartItem it = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(qty)
                .unitPrice(unitPrice)   // đã gồm topping
                .toppingsJson(topsJson) // snapshot topping
                .selected(true)
                .build();

        return itemRepo.save(it);
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
        itemRepo.findByCartIdAndId(cart.getId(), itemId).ifPresent(itemRepo::delete);
    }

    @Override
    public void clear(User user) {
        Cart cart = getOrCreate(user);
        itemRepo.deleteAll(itemRepo.findByCart(cart));
    }

    // =========================
    // Tính toán
    // =========================
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
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return subtotal.compareTo(BigDecimal.valueOf(200_000)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(20_000);
    }

    // =========================
    // Topping helpers + đổi topping
    // =========================
    private String normalizeToppingsJson(List<Long> toppingIds) {
        try {
            if (toppingIds == null || toppingIds.isEmpty()) return null;

            var tops = toppingRepo.findAllById(toppingIds);
            tops.sort(java.util.Comparator.comparing(net.codejava.utea.catalog.entity.Topping::getId)); // khoá gộp ổn định

            java.util.List<java.util.Map<String, Object>> arr = new java.util.ArrayList<>();
            for (var t : tops) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", t.getId());          // Long
                m.put("name", t.getName());      // String
                m.put("price", t.getPrice());    // BigDecimal
                arr.add(m);
            }
            return om.writeValueAsString(arr);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi build toppings json", e);
        }
    }

    private BigDecimal sumToppingPrice(List<Long> toppingIds) {
        if (toppingIds == null || toppingIds.isEmpty()) return BigDecimal.ZERO;
        return toppingRepo.findAllById(toppingIds).stream()
                .map(Topping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public CartItem updateToppings(User user, Long itemId, List<Long> toppingIds) {
        Cart cart = getOrCreate(user);
        CartItem item = itemRepo.findByCartIdAndId(cart.getId(), itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item"));

        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();

        // Danh mục Bánh (id=3) => không cho size và topping
        if (product.getCategory() != null && product.getCategory().getId() == 3L) {
            toppingIds = null;
        }

        BigDecimal base = (variant != null) ? variant.getPrice() : product.getBasePrice();
        BigDecimal topSum = sumToppingPrice(toppingIds);
        String topsJson = normalizeToppingsJson(toppingIds);
        BigDecimal newUnit = base.add(topSum);

        // Gộp vào dòng đã tồn tại (cùng p+v+topping) nếu có
        Optional<CartItem> existing;
        if (variant == null) {
            existing = (topsJson == null)
                    ? itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJsonIsNull(cart.getId(), product.getId())
                    : itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJson(cart.getId(), product.getId(), topsJson);
        } else {
            existing = (topsJson == null)
                    ? itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJsonIsNull(cart.getId(), product.getId(), variant.getId())
                    : itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJson(cart.getId(), product.getId(), variant.getId(), topsJson);
        }

        if (existing.isPresent() && !existing.get().getId().equals(item.getId())) {
            CartItem other = existing.get();
            other.setQuantity(other.getQuantity() + item.getQuantity());
            itemRepo.save(other);
            itemRepo.delete(item);
            return other;
        }

        item.setToppingsJson(topsJson);
        item.setUnitPrice(newUnit);
        return itemRepo.save(item);
    }
    @Override
    public void clearSelected(User user){
        listSelected(user).forEach(i -> removeItem(user, i.getId()));
    }
}
