package net.codejava.utea.view;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderSummary {
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal total;

    private String couponCode;
    private String couponMessage;
    private boolean couponApplied;
}