package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.service.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import net.codejava.utea.customer.entity.Cart;
import net.codejava.utea.customer.service.VariantService;
import net.codejava.utea.customer.entity.ProductVariant;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CustomerRepository customerRepo;
    private final VariantService variantService;

    private Customer currentCustomer(CustomUserDetails user) {
        if (user == null) throw new RuntimeException("Chưa đăng nhập");
        return customerRepo.findByAccount_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var c = currentCustomer(user);
        var items = cartService.getCartByCustomer(c);

        Map<Long, java.util.List<ProductVariant>> variantOptions = items.stream()
                .collect(Collectors.toMap(
                        Cart::getCartId,
                        it -> variantService.findActiveByProduct(it.getProduct().getProductId())
                ));

        model.addAttribute("items", items);
        model.addAttribute("variantOptions", variantOptions);
        model.addAttribute("subtotalAll", cartService.calculateSubtotalAll(c));
        model.addAttribute("subtotalSelected", cartService.calculateSubtotalSelected(c));
        model.addAttribute("useCustomerCSS", true);
        return "/customer/cart";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails user,
                      @RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) Long variantId,
                      @RequestParam(defaultValue = "/customer/menu") String redirect) {
        var customer = currentCustomer(user);
        if (variantId != null) cartService.addToCartWithVariant(customer, productId, variantId, quantity);
        else cartService.addToCart(customer, productId, quantity);
        return "redirect:" + ((redirect != null && !redirect.isBlank()) ? redirect : "/customer/cart");
    }

    @PostMapping("/update")
    public String update(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam(required=false) Long productId,
                         @RequestParam(required=false) Long variantId,
                         @RequestParam int quantity) {
        var c = currentCustomer(user);
        if (variantId != null) cartService.updateQuantityByVariant(c, variantId, quantity);
        else cartService.updateQuantity(c, productId, quantity);
        return "redirect:/customer/cart";
    }

    @PostMapping("/remove")
    public String remove(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam(required=false) Long productId,
                         @RequestParam(required=false) Long variantId) {
        var c = currentCustomer(user);
        if (variantId != null) cartService.removeFromCartByVariant(c, variantId);
        else cartService.removeFromCart(c, productId);
        return "redirect:/customer/cart";
    }

    @PostMapping("/select")
    public String selectOne(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam(required=false) Long productId,
                            @RequestParam(required=false) Long variantId,
                            @RequestParam boolean selected) {
        var c = currentCustomer(user);
        if (variantId != null) cartService.setSelectedByVariant(c, variantId, selected);
        else cartService.setSelectedByProductNoVariant(c, productId, selected);
        return "redirect:/customer/cart";
    }

    @PostMapping("/select-all")
    public String selectAll(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam boolean selected) {
        cartService.setSelectedAll(currentCustomer(user), selected);
        return "redirect:/customer/cart";
    }
    @PostMapping("/change-variant")
    public String changeVariant(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestParam Long cartId,
                                @RequestParam Long newVariantId) {
        var c = currentCustomer(user);
        cartService.changeVariant(c, cartId, newVariantId);
        return "redirect:/customer/cart";
    }

}
