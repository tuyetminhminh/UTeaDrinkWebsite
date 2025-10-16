package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.customer.entity.Order;
import net.codejava.utea.customer.entity.enums.OrderStatus;
import net.codejava.utea.customer.entity.enums.PaymentMethod;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.customer.service.CouponService;
import net.codejava.utea.customer.service.OrderService;
import net.codejava.utea.service.impl.CustomUserDetails;
import net.codejava.utea.view.OrderSummary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/customer/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final CustomerRepository customerRepo;

    private Customer currentCustomer(CustomUserDetails user) {
        return customerRepo.findByAccount_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    @GetMapping
    public String view(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var customer = currentCustomer(user);
        var subtotal = cartService.getSelectedSubtotal(customer);
        var shippingFee = cartService.estimateShippingFee(subtotal);

        var suggestions = couponService.suggest(subtotal, shippingFee);

        var req = new CheckoutRequest();
        req.setFullname(   nz(customer.getFullName()));
        req.setPhone(      nz(customer.getPhoneNumber()));
        req.setEmail(      nz(customer.getEmail()));
        req.setAddressLine(nz(customer.getDiachi()));
        // chưa có cột -> để trống (UI vẫn bind bình thường)
        req.setProvince("");
        req.setDistrict("");

        var summary = OrderSummary.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .total(subtotal.add(shippingFee))
                .couponApplied(false)
                .build();

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", new CheckoutRequest());
        model.addAttribute("suggestions", suggestions);
        return "customer/checkout";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@AuthenticationPrincipal CustomUserDetails user, @ModelAttribute("req") CheckoutRequest req, Model model) {
        var customer = currentCustomer(user);
        var subtotal = cartService.getSelectedSubtotal(customer);
        var shippingFee = cartService.estimateShippingFee(subtotal);

        var result = couponService.apply(req.getCouponCode(), subtotal, shippingFee);

        var summary = OrderSummary.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(result.discount())
                .total(result.total())
                .couponCode(req.getCouponCode())
                .couponApplied(result.ok())
                .couponMessage(result.message())
                .build();

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", req);
        model.addAttribute("suggestions", couponService.suggest(subtotal, shippingFee));
        return "customer/checkout";
    }

    @PostMapping
    public String place(@AuthenticationPrincipal CustomUserDetails user,
                        @ModelAttribute("req") CheckoutRequest req,
                        Model model) {
        var customer = currentCustomer(user);

        Order order = orderService.createFromCart(customer, builder -> builder
                        .status(OrderStatus.PENDING.name())
                        .paymentMethod(req.getPaymentMethod()),
                req);

        return "redirect:/customer/orders/" + order.getOrderCode();
    }
    private static String nz(String s) { return s == null ? "" : s; }
}
