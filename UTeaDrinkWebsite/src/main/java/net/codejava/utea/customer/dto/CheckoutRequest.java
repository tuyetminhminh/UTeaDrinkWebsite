package net.codejava.utea.customer.dto;

import lombok.*;
import net.codejava.utea.payment.entity.enums.PaymentMethod;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutRequest {
    private String fullname;
    private String phone;
    private String email;
    private String addressLine;
    private String province;
    private String district;
    private String note;



    private Long addressId;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String couponCode;
    private String shipCode;




}
