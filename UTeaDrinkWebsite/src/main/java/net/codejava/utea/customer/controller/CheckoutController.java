package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.customer.service.CouponService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping
    public String viewCheckout(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var subtotal = cartService.getSelectedSubtotal(user.getUser());
        var shipping = cartService.estimateShippingFee(subtotal);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("suggestCoupons", couponService.suggest(subtotal, shipping));
        return "customer/checkout";
    }

    @PostMapping
    public String doCheckout(@AuthenticationPrincipal CustomUserDetails user,
                             @RequestParam(required = false) String couponCode,
                             RedirectAttributes ra) {
        // gọi OrderService.createFromCart(...)
        // redirect -> /customer/orders
        return "redirect:/customer/orders";
    }
}