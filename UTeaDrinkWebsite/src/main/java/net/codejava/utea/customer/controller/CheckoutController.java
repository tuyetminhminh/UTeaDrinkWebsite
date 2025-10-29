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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                       @ModelAttribute("req") CheckoutRequest reqIn, @RequestParam(value="error", required=false) String payError) {
        var u = currentUser(user);
        var subtotal = cartService.getSelectedSubtotal(u);
        if (subtotal.signum() <= 0) return "redirect:/customer/cart";

        var shippingFee = cartService.estimateShippingFee(subtotal);

        // Áp dụng promotion tự động (không có voucher thì mới hiển thị promotion)
        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, u);
        
        // Build message cho promotion tự động
        String autoPromoMsg = null;
        if (autoPromos.hasAny()) {
            StringBuilder promoDetail = new StringBuilder();
            if (autoPromos.discount().ok()) {
                promoDetail.append(autoPromos.discount().message());
            }
            if (autoPromos.freeship().ok()) {
                if (promoDetail.length() > 0) promoDetail.append(" + ");
                promoDetail.append(autoPromos.freeship().message());
            }
            autoPromoMsg = "Áp dụng chương trình khuyến mãi: " + promoDetail.toString();
        }
        
        // Tạo object chứa thông tin promotion để hiển thị riêng
        var autoPromoInfo = new java.util.HashMap<String, Object>();
        autoPromoInfo.put("hasAny", autoPromos.hasAny());
        autoPromoInfo.put("discountMsg", autoPromos.discount().ok() ? autoPromos.discount().message() : null);
        autoPromoInfo.put("freeshipMsg", autoPromos.freeship().ok() ? autoPromos.freeship().message() : null);
        
        // Prefill địa chỉ mặc định
        var addresses = addressService.listOf(u);
        if ((reqIn.getFullname()==null || reqIn.getFullname().isBlank()) && !addresses.isEmpty()){
            var d = addresses.get(0);
            reqIn.setAddressId(d.getId());// ///////////////////////////////////////////////////////////
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
                .discountAmount(autoPromos.discount().discount()) // Giảm giá sản phẩm
                .shipDiscount(autoPromos.freeship().discount())   // Giảm phí ship
                .total(subtotal.add(shippingFee).subtract(autoPromos.totalDiscount()))
                .couponApplied(autoPromos.hasAny())
                .couponMessage(autoPromoMsg)
                .build();

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", reqIn);
        model.addAttribute("addresses", addresses);
        model.addAttribute("autoPromoInfo", autoPromoInfo);
        model.addAttribute("suggestionsPair", promotionService.suggestPair(subtotal, shippingFee));
        if (payError != null) {
            model.addAttribute("toastError", "Thanh toán thất bại. Bạn có thể chọn phương thức khác hoặc thử lại.");
        }
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

        // Áp dụng voucher (mã nhập) - ƯU TIÊN TRƯỚC
        TwoCouponsResult voucherResult = promotionService.applyBoth(req.getCouponCode(), req.getShipCode(), subtotal, shippingFee, u);
        
        // Áp dụng promotion tự động
        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, u);

        // Logic ưu tiên: MÃ GIẢM GIÁ trước, KHUYẾN MÃI sau (KHÔNG CỘNG DỒN)
        // 1. Giảm giá sản phẩm: Voucher HOẶC Promotion (chọn 1)
        BigDecimal productDiscount;
        String productDiscountMsg = null;
        String productDiscountSource = null; // "VOUCHER" hoặc "PROMOTION"
        
        if (voucherResult.okDiscount()) {
            // Có voucher giảm giá → dùng voucher, bỏ promotion
            productDiscount = voucherResult.discount();
            productDiscountMsg = voucherResult.msgDiscount();
            productDiscountSource = "VOUCHER";
        } else if (autoPromos.discount().ok()) {
            // Không có voucher → dùng promotion
            productDiscount = autoPromos.discount().discount();
            productDiscountMsg = autoPromos.discount().message();
            productDiscountSource = "PROMOTION";
        } else {
            productDiscount = BigDecimal.ZERO;
        }
        
        // 2. Freeship: Voucher HOẶC Promotion (chọn 1)
        BigDecimal freeshipDiscount;
        String freeshipMsg = null;
        String freeshipSource = null; // "VOUCHER" hoặc "PROMOTION"
        
        if (voucherResult.okShip()) {
            // Có voucher freeship → dùng voucher, bỏ promotion
            freeshipDiscount = voucherResult.shipDiscount();
            freeshipMsg = voucherResult.msgShip();
            freeshipSource = "VOUCHER";
        } else if (autoPromos.freeship().ok()) {
            // Không có voucher → dùng promotion freeship
            freeshipDiscount = autoPromos.freeship().discount();
            freeshipMsg = autoPromos.freeship().message();
            freeshipSource = "PROMOTION";
        } else {
            freeshipDiscount = BigDecimal.ZERO;
        }
        
        BigDecimal finalTotal = subtotal.add(shippingFee)
                .subtract(productDiscount)
                .subtract(freeshipDiscount)
                .max(BigDecimal.ZERO);
        
        // Build message đơn giản và rõ ràng
        StringBuilder msgBuilder = new StringBuilder();
        boolean hasVoucher = "VOUCHER".equals(productDiscountSource) || "VOUCHER".equals(freeshipSource);
        boolean hasPromotion = "PROMOTION".equals(productDiscountSource) || "PROMOTION".equals(freeshipSource);
        
        if (hasVoucher) {
            msgBuilder.append("✓ Áp dụng mã thành công.");
        }
        
        if (hasPromotion) {
            if (msgBuilder.length() > 0) msgBuilder.append(" | ");
            
            // Build chi tiết promotion
            StringBuilder promoDetail = new StringBuilder();
            if ("PROMOTION".equals(productDiscountSource) && productDiscountMsg != null) {
                promoDetail.append(productDiscountMsg);
            }
            if ("PROMOTION".equals(freeshipSource) && freeshipMsg != null) {
                if (promoDetail.length() > 0) promoDetail.append(" + ");
                promoDetail.append(freeshipMsg);
            }
            
            msgBuilder.append("Áp dụng chương trình khuyến mãi: ").append(promoDetail);
        }

        var summary = OrderSummary.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(productDiscount)
                .shipDiscount(freeshipDiscount)
                .total(finalTotal)
                .couponApplied(productDiscount.compareTo(BigDecimal.ZERO) > 0 || freeshipDiscount.compareTo(BigDecimal.ZERO) > 0)
                .couponMessage(msgBuilder.length() > 0 ? msgBuilder.toString() : null)
                .build();

        // Tạo object chứa thông tin promotion để hiển thị riêng (nếu không dùng voucher)
        var autoPromoInfo = new java.util.HashMap<String, Object>();
        boolean hasAutoPromo = false;
        String autoDiscountMsg = null;
        String autoFreeshipMsg = null;
        
        // Chỉ hiển thị promotion nếu KHÔNG dùng voucher cho loại đó
        if (!"VOUCHER".equals(productDiscountSource) && autoPromos.discount().ok()) {
            hasAutoPromo = true;
            autoDiscountMsg = autoPromos.discount().message();
        }
        if (!"VOUCHER".equals(freeshipSource) && autoPromos.freeship().ok()) {
            hasAutoPromo = true;
            autoFreeshipMsg = autoPromos.freeship().message();
        }
        
        autoPromoInfo.put("hasAny", hasAutoPromo);
        autoPromoInfo.put("discountMsg", autoDiscountMsg);
        autoPromoInfo.put("freeshipMsg", autoFreeshipMsg);

        model.addAttribute("summary", summary);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("req", req);
        model.addAttribute("addresses", addressService.listOf(u));
        model.addAttribute("autoPromoInfo", autoPromoInfo);
        model.addAttribute("suggestionsPair", promotionService.suggestPair(subtotal, shippingFee));
        return "customer/checkout";
    }

    @PostMapping
    public String place(@AuthenticationPrincipal CustomUserDetails user,
                        @ModelAttribute("req") CheckoutRequest req, RedirectAttributes ra, Model model) {
        var u = currentUser(user);
        var subtotal = cartService.getSelectedSubtotal(u);
        if (subtotal.signum() <= 0) return "redirect:/customer/cart";
        var shippingFee = cartService.estimateShippingFee(subtotal);

        // ✅ VALIDATION ĐỊA CHỈ - Kiểm tra xem user đã nhập đầy đủ thông tin chưa
        boolean hasAddress = (req.getFullname() != null && !req.getFullname().isBlank()) &&
                             (req.getPhone() != null && !req.getPhone().isBlank()) &&
                             (req.getAddressLine() != null && !req.getAddressLine().isBlank()) &&
                             (req.getDistrict() != null && !req.getDistrict().isBlank()) &&
                             (req.getProvince() != null && !req.getProvince().isBlank());
        
        if (!hasAddress) {
            ra.addFlashAttribute("toastError", "Vui lòng điền đầy đủ thông tin giao hàng!");
            return "redirect:/customer/checkout";
        }

        // Tính tổng giá cuối cùng - Logic ưu tiên: Voucher trước, Promotion sau
        var voucherResult = promotionService.applyBoth(req.getCouponCode(), req.getShipCode(), subtotal, shippingFee, u);
        var autoPromos = promotionService.findBestAutoPromotions(subtotal, shippingFee, u);
        
        // Giảm giá sản phẩm: Voucher HOẶC Promotion (ưu tiên voucher)
        BigDecimal productDiscount = voucherResult.okDiscount() 
                ? voucherResult.discount() 
                : autoPromos.discount().discount();
        
        // Freeship: Voucher HOẶC Promotion (ưu tiên voucher)
        BigDecimal freeshipDiscount = voucherResult.okShip() 
                ? voucherResult.shipDiscount() 
                : autoPromos.freeship().discount();
        
//        var finalTotal = subtotal.add(shippingFee)
//                .subtract(productDiscount)
//                .subtract(freeshipDiscount)
//                .max(BigDecimal.ZERO);

        var order = orderService.createFromCart(
                u,
                builder -> builder.status(OrderStatus.NEW),
                req
        );

        if (req.getPaymentMethod() == PaymentMethod.COD) {
            cartService.clearSelected(u);
            ra.addFlashAttribute("toastSuccess", "Đặt hàng thành công. Vui lòng thanh toán khi nhận hàng.");
            return "redirect:/customer/orders/thank-you?order=" + order.getOrderCode();
        }

        payRepo.save(PaymentTransaction.builder()
                .orderCode(order.getOrderCode())
                .method(req.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(order.getTotal())
                .build());

        String methodPath = req.getPaymentMethod().name().toLowerCase(); // momo | vnpay
        return "redirect:/customer/pay/" + methodPath + "/" + order.getOrderCode();
    }
}
