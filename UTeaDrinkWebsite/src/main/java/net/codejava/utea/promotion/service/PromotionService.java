package net.codejava.utea.promotion.service;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    /** Gợi ý 1 danh sách mã “tốt nhất” dựa trên subtotal + shipping (không phân loại). */
    List<PromotionSuggestion> suggest(BigDecimal subtotal, BigDecimal shipping);

    /** Áp 1 mã đơn lẻ (tiền/% hoặc freeship). */
    PromotionResult applyVoucher(String code, BigDecimal subtotal, BigDecimal shipping);

    /** Trả về tổng khi KHÔNG có mã (tiện dùng). */
    default PromotionResult empty(BigDecimal subtotal, BigDecimal shipping) {
        return new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping));
    }

    /** Áp đồng thời 2 mã: 1 mã giảm tiền/% + 1 mã freeship. */
    TwoCouponsResult applyBoth(String codeDiscount, String codeShip,
                               BigDecimal subtotal, BigDecimal shipping);

    /**
     * Gợi ý tách theo 2 nhóm:
     *  - discount: mã giảm tiền/%
     *  - ship    : mã freeship/giảm ship
     */
    SuggestionsPair suggestPair(BigDecimal subtotal, BigDecimal shipping);
}
