package net.codejava.utea.customer.service;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    record ApplyResult(boolean ok, BigDecimal discount, BigDecimal total, String message) {}
    ApplyResult apply(String code, BigDecimal subtotal, BigDecimal shippingFee);

    record Suggestion(String code, String title, String note) {}
    List<Suggestion> suggest(BigDecimal subtotal, BigDecimal shippingFee);
}
