package net.codejava.utea.promotion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoType;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.promotion.service.PromotionResult;
import net.codejava.utea.promotion.service.PromotionService;
import net.codejava.utea.promotion.service.PromotionSuggestion;
import net.codejava.utea.promotion.service.model.PromoRule;
import net.codejava.utea.promotion.service.TwoCouponsResult;
import net.codejava.utea.promotion.service.SuggestionsPair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final VoucherRepository voucherRepo;
    private final PromotionRepository promotionRepo;
    private final ObjectMapper objectMapper;

    @Override
    public List<PromotionSuggestion> suggest(BigDecimal subtotal, BigDecimal shipping) {
        var now = LocalDateTime.now();
        var vouchers = voucherRepo.findActiveNow(now);

        // Tính mức giảm “tiềm năng” để sắp xếp gợi ý
        return vouchers.stream()
                .map(v -> new SuggestProbe(v.getCode(),
                        potentialDiscount(v, subtotal, shipping)))
                .filter(p -> p.potential().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(SuggestProbe::potential).reversed())
                .limit(10)
                // Dùng code làm title. Note có thể trống hoặc tự build từ rule nếu muốn.
                .map(p -> new PromotionSuggestion(p.code(), p.code(), ""))
                .toList();
    }

    @Override
    public PromotionResult applyVoucher(String code, BigDecimal subtotal, BigDecimal shipping) {
        if (code == null || code.isBlank()) {
            return new PromotionResult(false, "Vui lòng nhập mã.", BigDecimal.ZERO, subtotal.add(shipping));
        }
        var opt = voucherRepo.findByCodeActiveNow(code.trim(), LocalDateTime.now());
        if (opt.isEmpty()) {
            return new PromotionResult(false, "Mã không hợp lệ hoặc đã hết hạn.", BigDecimal.ZERO, subtotal.add(shipping));
        }
        var v = opt.get();
        var rule = readRuleSafe(v.getRuleJson());
        var discount = computeDiscountByRule(rule, subtotal, shipping, null);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PromotionResult(false, "Mã không đáp ứng điều kiện.", BigDecimal.ZERO, subtotal.add(shipping));
        }
        var total = subtotal.add(shipping).subtract(discount).max(BigDecimal.ZERO);
        return new PromotionResult(true, "Áp dụng mã thành công.", discount, total);
    }

    // ---------- helpers ----------
    private BigDecimal potentialDiscount(Voucher v, BigDecimal subtotal, BigDecimal shipping) {
        return computeDiscountByRule(readRuleSafe(v.getRuleJson()), subtotal, shipping, null);
    }

    private PromoRule readRuleSafe(String json) {
        if (json == null || json.isBlank()) return new PromoRule();
        try { return objectMapper.readValue(json, PromoRule.class); }
        catch (Exception e) { return new PromoRule(); }
    }

    private BigDecimal computeDiscountByRule(PromoRule rule,
                                             BigDecimal subtotal,
                                             BigDecimal shipping,
                                             PromoType typeIfPromotion) {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (shipping == null) shipping = BigDecimal.ZERO;
        if (rule == null) return BigDecimal.ZERO;

        if (rule.getMinTotal() != null && subtotal.compareTo(rule.getMinTotal()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal best = BigDecimal.ZERO;

        // FREESHIP
        if (Boolean.TRUE.equals(rule.getFreeShip())) {
            best = max(best, clamp(shipping));
        }

        // PERCENT
        if (rule.getPercentOff() != null && rule.getPercentOff() > 0) {
            var pct = BigDecimal.valueOf(rule.getPercentOff());
            var byPct = subtotal.multiply(pct).divide(BigDecimal.valueOf(100));
            if (rule.getAmountCap() != null && rule.getAmountCap().compareTo(BigDecimal.ZERO) > 0) {
                byPct = byPct.min(rule.getAmountCap());
            }
            best = max(best, clamp(byPct));
        }

        // AMOUNT
        if (rule.getAmountOff() != null && rule.getAmountOff().compareTo(BigDecimal.ZERO) > 0) {
            best = max(best, rule.getAmountOff());
        }

        if (typeIfPromotion != null) {
            return switch (typeIfPromotion) {
                case FREESHIP -> clamp(Boolean.TRUE.equals(rule.getFreeShip()) ? shipping : BigDecimal.ZERO);
                case PERCENT  -> {
                    if (rule.getPercentOff() == null || rule.getPercentOff() <= 0) yield BigDecimal.ZERO;
                    var byPct = subtotal.multiply(BigDecimal.valueOf(rule.getPercentOff()))
                            .divide(BigDecimal.valueOf(100));
                    if (rule.getAmountCap() != null && rule.getAmountCap().compareTo(BigDecimal.ZERO) > 0) {
                        byPct = byPct.min(rule.getAmountCap());
                    }
                    yield clamp(byPct);
                }
                case AMOUNT   -> clamp(rule.getAmountOff() == null ? BigDecimal.ZERO : rule.getAmountOff());
            };
        }

        return best;
    }

    private static BigDecimal clamp(BigDecimal v) { return v == null || v.signum() < 0 ? BigDecimal.ZERO : v; }
    private static BigDecimal max(BigDecimal a, BigDecimal b) { return a.compareTo(b) >= 0 ? a : b; }

    private record SuggestProbe(String code, BigDecimal potential) {}

    @Override
    public TwoCouponsResult applyBoth(String codeDiscount, String codeShip,
                                      BigDecimal subtotal, BigDecimal shipping) {
        // discount
        var r1 = (codeDiscount == null || codeDiscount.isBlank())
                ? new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping))
                : applyVoucher(codeDiscount, subtotal, shipping);

        // ship
        var r2 = (codeShip == null || codeShip.isBlank())
                ? new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping))
                : applyVoucher(codeShip, subtotal, shipping);

        var discount = r1.discount();
        var shipDiscount = r2.discount().min(shipping.max(BigDecimal.ZERO)); // clamp <= shipping
        var total = subtotal.add(shipping).subtract(discount).subtract(shipDiscount);
        if (total.signum() < 0) total = BigDecimal.ZERO;

        return new TwoCouponsResult(
                r1.ok(), r1.message(), discount,
                r2.ok(), r2.message(), shipDiscount,
                total
        );
    }

    @Override
    public SuggestionsPair suggestPair(BigDecimal subtotal, BigDecimal shipping) {
        var all = suggest(subtotal, shipping);
        // heuristic đơn giản: nếu title/note chứa "ship"/"freeship" → nhóm ship, còn lại nhóm discount
        var shipList = all.stream()
                .filter(s -> (s.title()!=null && s.title().toLowerCase().contains("ship"))
                        || (s.note()!=null  && s.note().toLowerCase().contains("ship")))
                .toList();
        var discList = all.stream()
                .filter(s -> !shipList.contains(s))
                .toList();
        return new SuggestionsPair(discList, shipList);
    }
}

