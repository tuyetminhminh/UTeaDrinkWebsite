// net.codejava.utea.customer.controller.CartController
package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.customer.service.VariantService;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final VariantService variantService;

    private User currentUser(CustomUserDetails user) {
        if (user == null) throw new RuntimeException("Chưa đăng nhập");
        // CustomUserDetails thường có id/email/roles; giả sử bạn đã map 1-1 với User
        // Nếu cần, inject UserRepository để .findById(user.getId())
        User u = new User();
        u.setId(user.getId()); // hoặc nạp từ repo
        return u;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var u = currentUser(user);
        var items = cartService.listItems(u);

        // Map<itemId, List<Variant>> để hiện dropdown đổi size cho từng dòng có variant
        Map<Long, java.util.List<ProductVariant>> variantOptions = items.stream()
                .filter(i -> i.getVariant() != null) // chỉ item có variant mới cần chọn size
                .collect(Collectors.toMap(
                        CartItem::getId,
                        it -> variantService.findActiveByProduct(it.getProduct().getId())
                ));

        model.addAttribute("items", items);
        model.addAttribute("variantOptions", variantOptions);
        model.addAttribute("subtotalAll", cartService.getSubtotal(u));
        model.addAttribute("subtotalSelected", cartService.getSelectedSubtotal(u));
        model.addAttribute("useCustomerCSS", true);
        return "customer/cart";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails user,
                      @RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) Long variantId,
                      @RequestParam(defaultValue = "/customer/menu") String redirect) {
        var u = currentUser(user);
        cartService.addItem(u, productId, variantId, Math.max(1, quantity));
        return "redirect:" + (redirect != null && !redirect.isBlank() ? redirect : "/customer/cart");
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
        // không cần service riêng: có thể lặp items để set; nhưng bạn có thể thêm hàm tiện
        cartService.listItems(u).forEach(i -> cartService.toggleSelect(u, i.getId(), selected));
        return "redirect:/customer/cart";
    }

    // Đổi size của một dòng (đổi variant)
    @PostMapping("/change-variant")
    public String changeVariant(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestParam Long itemId,
                                @RequestParam Long newVariantId) {
        var u = currentUser(user);
        // tái sử dụng add+remove (cách đơn giản): lấy item cũ, xóa, add item mới
        var old = cartService.listItems(u).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow(() -> new RuntimeException("Item not found"));

        // remove dòng cũ
        cartService.removeItem(u, itemId);
        // add dòng mới (giữ số lượng cũ)
        cartService.addItem(u, old.getProduct().getId(), newVariantId, old.getQuantity());

        return "redirect:/customer/cart";
    }

}
