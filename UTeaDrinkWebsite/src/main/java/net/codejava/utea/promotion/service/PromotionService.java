package net.codejava.utea.promotion.service;

import net.codejava.utea.common.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    /** Gợi ý 1 danh sách mã "tốt nhất" dựa trên subtotal + shipping (không phân loại). */
    List<PromotionSuggestion> suggest(BigDecimal subtotal, BigDecimal shipping);

    /** Áp 1 mã đơn lẻ (tiền/% hoặc freeship). Cần User để kiểm tra forFirstOrder và forBirthday. */
    PromotionResult applyVoucher(String code, BigDecimal subtotal, BigDecimal shipping, User user);

    /** Trả về tổng khi KHÔNG có mã (tiện dùng). */
    default PromotionResult empty(BigDecimal subtotal, BigDecimal shipping) {
        return new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping));
    }

    /** Áp đồng thời 2 mã: 1 mã giảm tiền/% + 1 mã freeship. Cần User để kiểm tra forFirstOrder và forBirthday. */
    TwoCouponsResult applyBoth(String codeDiscount, String codeShip,
                               BigDecimal subtotal, BigDecimal shipping, User user);

    /**
     * Gợi ý tách theo 2 nhóm:
     *  - discount: mã giảm tiền/%
     *  - ship    : mã freeship/giảm ship
     */
    SuggestionsPair suggestPair(BigDecimal subtotal, BigDecimal shipping);

    /**
     * Tăng số lần sử dụng voucher (usedCount) lên 1.
     * Tự động chuyển status sang EXHAUSTED nếu đã đạt usageLimit.
     */
    void incrementVoucherUsage(String code);

    /**
     * Tìm chương trình khuyến mãi tự động tốt nhất (không cần nhập mã).
     * Trả về promotion có giá trị giảm giá lớn nhất phù hợp với đơn hàng.
     * @deprecated Sử dụng findBestAutoPromotions() để khách hàng được hưởng cả discount và freeship
     */
    @Deprecated
    PromotionResult findBestAutoPromotion(BigDecimal subtotal, BigDecimal shipping, User user);

    /**
     * Tìm chương trình khuyến mãi tự động tốt nhất cho TỪNG LOẠI.
     * Khách hàng có thể hưởng CẢ HAI: promotion giảm giá sản phẩm + promotion freeship.
     * VD: Giảm 26% sản phẩm + Miễn phí ship
     */
    AutoPromotionResult findBestAutoPromotions(BigDecimal subtotal, BigDecimal shipping, User user);
}
