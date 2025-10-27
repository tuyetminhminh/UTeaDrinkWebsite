package net.codejava.utea.promotion.service;

import java.math.BigDecimal;

/**
 * Kết quả áp dụng chương trình khuyến mãi tự động.
 * Tách riêng: promotion giảm giá sản phẩm + promotion freeship.
 * Khách hàng có thể hưởng CẢ HAI promotions cùng lúc.
 */
public record AutoPromotionResult(
        PromotionResult discount,   // promotion giảm giá sản phẩm (PERCENT/AMOUNT)
        PromotionResult freeship    // promotion freeship
) {
    /**
     * Tổng giảm giá từ cả 2 promotions
     */
    public BigDecimal totalDiscount() {
        return discount.discount().add(freeship.discount());
    }

    /**
     * Có promotion nào được áp dụng không?
     */
    public boolean hasAny() {
        return discount.ok() || freeship.ok();
    }

    /**
     * Message hiển thị cho user
     */
    public String displayMessage() {
        if (!hasAny()) return null;
        
        StringBuilder msg = new StringBuilder();
        if (discount.ok() && discount.message() != null) {
            msg.append(discount.message());
        }
        if (freeship.ok() && freeship.message() != null) {
            if (msg.length() > 0) msg.append(" + ");
            msg.append(freeship.message());
        }
        return msg.toString();
    }
}

