package net.codejava.utea.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.customer.service.VariantService;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.catalog.service.ToppingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final VariantService variantService;
    private final ProductVariantRepository variantRepo;
    private final ProductRepository productRepo;
    private final ToppingService toppingService;
    private final ObjectMapper om = new ObjectMapper();

    private User currentUser(CustomUserDetails cud) {
        if (cud == null) throw new RuntimeException("Chưa đăng nhập");
        User u = new User();
        u.setId(cud.getUser().getId()); // ✅ lấy id từ user bên trong
        return u;
    }


    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var u = currentUser(user);
        var items = new ArrayList<>(cartService.listItems(u));

        // SẮP XẾP MỚI NHẤT TRƯỚC (ưu tiên createdAt; nếu không có thì theo id)
        items.sort((a, b) -> {
            try {
                var ca = a.getCreatedAt();
                var cb = b.getCreatedAt();
                if (ca != null && cb != null) return cb.compareTo(ca);
            } catch (Exception ignore) {}
            return Long.compare(
                    b.getId() == null ? 0L : b.getId(),
                    a.getId() == null ? 0L : a.getId());
        });

        // Dropdown đổi size theo từng item (nếu có variant)
        Map<Long, List<ProductVariant>> variantOptions = items.stream()
                .filter(i -> i.getVariant() != null)
                .collect(Collectors.toMap(
                        CartItem::getId,
                        it -> variantService.findActiveByProduct(it.getProduct().getId())
                ));

        model.addAttribute("items", items);
        model.addAttribute("variantOptions", variantOptions);
        model.addAttribute("subtotalAll", cartService.getSubtotal(u));
        model.addAttribute("subtotalSelected", cartService.getSelectedSubtotal(u));
        model.addAttribute("useCustomerCSS", true);

        // JSON TOPPING THEO SHOP: { shopId: [ {id,name,price}, ... ] }
        Map<Long, List<Map<String, Object>>> topsMap = new LinkedHashMap<>();
        for (CartItem it : items) {
            var shop = (it.getProduct() != null) ? it.getProduct().getShop() : null;
            if (shop == null) continue;
            long shopId = shop.getId();
            if (!topsMap.containsKey(shopId)) {
                var tops = toppingService.getToppingsForShop(shopId);
                var arr = tops.stream().map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("name", t.getName());
                    m.put("price", t.getPrice());
                    return m;
                }).toList();
                topsMap.put(shopId, arr);
            }
        }
        String topsDataByShopJson = "{}";
        try { topsDataByShopJson = om.writeValueAsString(topsMap); } catch (Exception ignore) {}
        model.addAttribute("topsDataByShopJson", topsDataByShopJson);

        return "customer/cart";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails user,
                      @RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) Long variantId,
                      @RequestParam(required = false, name = "toppingIds") List<Long> toppingIds,
                      @RequestParam(required = false) String redirect,
                      org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        var u = currentUser(user);
        var p = productRepo.findById(productId).orElseThrow();

        // Danh mục Bánh (id=3) => không có size và topping
        if (p.getCategory() != null && p.getCategory().getId() == 3L) {
            variantId = null;
            toppingIds = null;
        }

        cartService.addItem(u, productId, variantId, Math.max(1, quantity), toppingIds);
        ra.addFlashAttribute("msg", "Đã thêm \"" + p.getName() + "\" vào giỏ hàng.");

        String target = redirect;
        if (target == null || target.isBlank() || "/customer/menu".equals(target)) {
            target = "/customer/product/" + productId;
        }
        return "redirect:" + target;
    }

    @PostMapping("/update")
    public String update(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam Long itemId,
                         @RequestParam int quantity) {
        var u = currentUser(user);
        cartService.updateQty(u, itemId, Math.max(1, quantity));
        return "redirect:/customer/cart";
    }

    @PostMapping("/remove")
    public String remove(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam Long itemId) {
        var u = currentUser(user);
        cartService.removeItem(u, itemId);
        return "redirect:/customer/cart";
    }

    @PostMapping("/select")
    public String selectOne(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam Long itemId,
                            @RequestParam boolean selected) {
        var u = currentUser(user);
        cartService.toggleSelect(u, itemId, selected);
        return "redirect:/customer/cart";
    }

    @PostMapping("/select-all")
    public String selectAll(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam boolean selected) {
        var u = currentUser(user);
        cartService.listItems(u).forEach(i -> cartService.toggleSelect(u, i.getId(), selected));
        return "redirect:/customer/cart";
    }

    @PostMapping("/change-variant")
    public String changeVariant(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestParam Long itemId,
                                @RequestParam Long newVariantId) {
        var u = currentUser(user);

        var old = cartService.listItems(u).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow(() -> new RuntimeException("Item not found"));

        List<Long> toppingIds = null;
        try {
            if (old.getToppingsJson() != null && !old.getToppingsJson().isBlank()) {
                var arr = om.readValue(old.getToppingsJson(), java.util.List.class);
                toppingIds = (List<Long>) ((List<?>) arr).stream()
                        .map(m -> ((Number) ((Map<?, ?>) m).get("id")).longValue())
                        .toList();
            }
        } catch (Exception ignore) {}

        cartService.removeItem(u, itemId);
        cartService.addItem(u, old.getProduct().getId(), newVariantId, old.getQuantity(), toppingIds);

        return "redirect:/customer/cart";
    }

    @PostMapping("/change-toppings")
    public String changeToppings(@AuthenticationPrincipal CustomUserDetails user,
                                 @RequestParam Long itemId,
                                 @RequestParam(required = false, name = "toppingIds") List<Long> toppingIds) {
        var u = currentUser(user);
        cartService.updateToppings(u, itemId, toppingIds);
        return "redirect:/customer/cart";
    }
}
