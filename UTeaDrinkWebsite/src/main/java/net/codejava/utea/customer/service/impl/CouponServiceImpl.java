// src/main/java/net/codejava/utea/service/impl/CouponServiceImpl.java
package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.entity.Coupon;
import net.codejava.utea.customer.repository.CouponRepository;
import net.codejava.utea.customer.service.CouponService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository repo;

    @Override
    public ApplyResult apply(String code, BigDecimal subtotal, BigDecimal shippingFee) {
        if (code == null || code.isBlank()) {
            var total = subtotal.add(shippingFee);
            return new ApplyResult(false, BigDecimal.ZERO, total, null);
        }
        var opt = repo.findByCodeActiveNow(code.trim());
        if (opt.isEmpty()) {
            var total = subtotal.add(shippingFee);
            return new ApplyResult(false, BigDecimal.ZERO, total, "Mã không hợp lệ hoặc hết hạn");
        }
        Coupon c = opt.get();

        // Kiểm tra min order
        if (c.getMinOrderValue() != null && subtotal.compareTo(c.getMinOrderValue()) < 0) {
            var total = subtotal.add(shippingFee);
            return new ApplyResult(false, BigDecimal.ZERO, total,
                    "Đơn chưa đạt tối thiểu " + c.getMinOrderValue());
        }

        BigDecimal discount = BigDecimal.ZERO;
        switch (c.getType()) {
            case ORDER_PERCENT -> discount = subtotal.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100));
            case ORDER_AMOUNT  -> discount = c.getDiscountValue();
            case SHIPPING_PERCENT -> {
                var shipDisc = shippingFee.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100));
                discount = shipDisc.min(shippingFee); // không vượt quá phí ship
            }
            case SHIPPING_AMOUNT -> discount = c.getDiscountValue().min(shippingFee);
        }
        if (c.getMaxDiscount() != null && discount.compareTo(c.getMaxDiscount()) > 0) {
            discount = c.getMaxDiscount();
        }

        BigDecimal total = subtotal.add(shippingFee)
                .subtract(discount)
                .max(BigDecimal.ZERO);

        return new ApplyResult(true, discount.max(BigDecimal.ZERO), total, "Mã " + c.getCode() + " đã được áp dụng");
    }
    @Override
    public List<Suggestion> suggest(BigDecimal subtotal, BigDecimal shippingFee) {
        return repo.findAllActiveNow().stream()
                .filter(c -> c.getMinOrderValue() == null || subtotal.compareTo(c.getMinOrderValue()) >= 0)
                .limit(5) // gợi ý tối đa 5 mã
                .map(c -> {
                    String note = switch (c.getType()) {
                        case ORDER_PERCENT -> "Giảm " + c.getDiscountValue() + "% cho đơn";
                        case ORDER_AMOUNT -> "Giảm " + c.getDiscountValue().toPlainString() + "đ cho đơn";
                        case SHIPPING_PERCENT -> "Giảm " + c.getDiscountValue() + "% phí ship";
                        case SHIPPING_AMOUNT -> "Giảm " + c.getDiscountValue().toPlainString() + "đ phí ship";
                    };
                    if (c.getMaxDiscount() != null) note += " (tối đa " + c.getMaxDiscount().toPlainString() + "đ)";
                    return new Suggestion(c.getCode(), c.getTitle(), note);
                })
                .toList();
    }
}
