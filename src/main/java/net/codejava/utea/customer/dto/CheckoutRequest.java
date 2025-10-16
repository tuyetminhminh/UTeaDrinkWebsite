package net.codejava.utea.customer.dto;

import lombok.*;
import net.codejava.utea.customer.entity.enums.PaymentMethod;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutRequest {
    private String fullname;
    private String phone;
    private String email;
    private String addressLine;
    private String province;
    private String district;
    private String note;

    private PaymentMethod paymentMethod;

    // Coupon
    private String couponCode;
}