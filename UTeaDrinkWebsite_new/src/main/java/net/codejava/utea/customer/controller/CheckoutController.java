package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.AddressService;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.customer.service.CartService;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.payment.entity.PaymentTransaction;
import net.codejava.utea.payment.entity.enums.PaymentMethod;
import net.codejava.utea.payment.entity.enums.PaymentStatus;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
import net.codejava.utea.promotion.service.PromotionService;
import net.codejava.utea.promotion.service.TwoCouponsResult;
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
    private final PromotionService promotionService;
    private final OrderService orderService;
    private final PaymentTransactionRepository payRepo;
    private final AddressService addressService;

    private User currentUser(CustomUserDetails cud){
        if (cud == null) throw new RuntimeException("Chưa đăng nhập");
        User u = new User(); u.setId(cud.getId()); return u;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal CustomUserDetails user, Model model,
                       @ModelAttribute("req") CheckoutRequest reqIn) {
        var u = currentUser(user);
        var subtotal = cartService.getSelectedSubtotal(u);
        if (subtotal.signum() <= 0) return "redirect:/customer/cart";

        var shippingFee = cartService.estimateShippingFee(subtotal);

        // Prefill địa chỉ mặc định
        var addresses = addressService.listOf(u);
        if ((reqIn.getFullname()==null || reqIn.getFullname().isBlank()) && !addresses.isEmpty()){
            var d = addresses.get(0);
            reqIn.setFullname(d.getReceiverName());
            reqIn.setPhone(d.getPhone());
            reqIn.setAddressLine(d.getLine());
            reqIn.setDistrict(d.getDistrict());
            reqIn.setProvince(d.getProvince());
        }
        if (reqIn.getPaymentMethod()==null) reqIn.setPaymentMethod(PaymentMethod.COD);

        var summary = OrderSummary.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .shipDiscount(BigDecimal.ZERO)
                .total(subtotal.add(shippingFee))
                .couponApplied(false)
                .build();

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", reqIn);
        model.addAttribute("addresses", addresses);
        model.addAttribute("suggestionsPair", promotionService.suggestPair(subtotal, shippingFee));
        return "customer/checkout";
    }

    @PostMapping("/apply-coupons")
    public String applyCoupons(@AuthenticationPrincipal CustomUserDetails user,
                               @ModelAttribute("req") CheckoutRequest req,
                               Model model) {
        var u = currentUser(user);
        var subtotal = cartService.getSelectedSubtotal(u);
        if (subtotal.signum() <= 0) return "redirect:/customer/cart";
        var shippingFee = cartService.estimateShippingFee(subtotal);

        TwoCouponsResult result = promotionService.applyBoth(req.getCouponCode(), req.getShipCode(), subtotal, shippingFee);

        var summary = OrderSummary.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(result.discount())
                .shipDiscount(result.shipDiscount())
                .total(result.total())
                .couponApplied(result.okDiscount() || result.okShip())
                .couponMessage(
                        (result.msgDiscount()==null?"":result.msgDiscount()) +
                                ((result.msgDiscount()!=null && result.msgShip()!=null) ? " · " : "") +
                                (result.msgShip()==null?"":result.msgShip())
                )
                .build();

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", req);
        model.addAttribute("addresses", addressService.listOf(u));
        model.addAttribute("suggestionsPair", promotionService.suggestPair(subtotal, shippingFee));
        return "customer/checkout";
    }

    @PostMapping
    public String place(@AuthenticationPrincipal CustomUserDetails user,
                        @ModelAttribute("req") CheckoutRequest req) {
        var u = currentUser(user);
        var subtotal = cartService.getSelectedSubtotal(u);
        if (subtotal.signum() <= 0) return "redirect:/customer/cart";
        var shippingFee = cartService.estimateShippingFee(subtotal);

        var both = promotionService.applyBoth(req.getCouponCode(), req.getShipCode(), subtotal, shippingFee);
        var finalTotal = both.total();

        var order = orderService.createFromCart(
                u,
                builder -> builder.status(OrderStatus.NEW),
                req
        );

        if (req.getPaymentMethod() == PaymentMethod.COD) {
            return "redirect:/customer/orders/thank-you?order=" + order.getOrderCode();
        }

        payRepo.save(PaymentTransaction.builder()
                .orderCode(order.getOrderCode())
                .method(req.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(finalTotal)
                .build());

        return "redirect:/customer/pay/" + order.getOrderCode() + "?method=" + req.getPaymentMethod();
    }
}
