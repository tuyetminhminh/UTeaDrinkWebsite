package net.codejava.utea.view;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderSummary {
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal shipDiscount;
    private BigDecimal total;

    private boolean   couponApplied;
    private String    couponMessage;
}