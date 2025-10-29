package net.codejava.utea.promotion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoType;
import net.codejava.utea.promotion.repository.CustomerVoucherRepository;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.promotion.service.AutoPromotionResult;
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
    private final OrderRepository orderRepo;
    private final CustomerVoucherRepository customerVoucherRepo; // ⭐ THÊM MỚI
    private final ObjectMapper objectMapper;

    @Override
    public List<PromotionSuggestion> suggest(BigDecimal subtotal, BigDecimal shipping) {
        var now = LocalDateTime.now();
        var vouchers = voucherRepo.findActiveNow(now);

        // Tính mức giảm "tiềm năng" để sắp xếp gợi ý
        return vouchers.stream()
                .map(v -> {
                    var rule = readRuleSafe(v.getRuleJson());
                    var discount = potentialDiscount(v, subtotal, shipping);
                    
                    // Tạo note mô tả voucher dựa vào rule
                    String note = buildVoucherNote(rule);
                    
                    return new SuggestProbeWithNote(v.getCode(), discount, note, rule);
                })
                .filter(p -> p.potential().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(SuggestProbeWithNote::potential).reversed())
                .limit(10)
                .map(p -> new PromotionSuggestion(p.code(), p.code(), p.note()))
                .toList();
    }
    
    private String buildVoucherNote(PromoRule rule) {
        if (Boolean.TRUE.equals(rule.getFreeShip())) {
            return "Miễn phí ship";
        } else if (rule.getAmountOff() != null && rule.getAmountOff().compareTo(BigDecimal.ZERO) > 0) {
            return "Giảm " + formatMoney(rule.getAmountOff());
        } else if (rule.getPercentOff() != null && rule.getPercentOff() > 0) {
            String base = "Giảm " + rule.getPercentOff() + "%";
            if (rule.getAmountCap() != null && rule.getAmountCap().compareTo(BigDecimal.ZERO) > 0) {
                base += " (tối đa " + formatMoney(rule.getAmountCap()) + ")";
            }
            return base;
        }
        return "";
    }
    
    private String formatMoney(BigDecimal amount) {
        return String.format("%,dđ", amount.longValue());
    }

    @Override
    public PromotionResult applyVoucher(String code, BigDecimal subtotal, BigDecimal shipping, User user) {
        if (code == null || code.isBlank()) {
            return new PromotionResult(false, "Vui lòng nhập mã.", BigDecimal.ZERO, subtotal.add(shipping));
        }
        var opt = voucherRepo.findByCodeActiveNow(code.trim(), LocalDateTime.now());
        if (opt.isEmpty()) {
            return new PromotionResult(false, "Mã không hợp lệ hoặc đã hết hạn.", BigDecimal.ZERO, subtotal.add(shipping));
        }
        var v = opt.get();
        
        // ⭐ KIỂM TRA: User đã lưu voucher hay chưa?
        if (user != null) {
            var savedVoucher = customerVoucherRepo.findByUser_IdAndVoucher_CodeAndState(
                    user.getId(), code.trim(), "ACTIVE"
            );
            
            if (savedVoucher.isEmpty()) {
                return new PromotionResult(false, 
                    "Bạn cần LƯU voucher này trước khi sử dụng. Vào trang Voucher để lưu mã.", 
                    BigDecimal.ZERO, subtotal.add(shipping));
            }
        }
        
        // Kiểm tra điều kiện forFirstOrder: chỉ cho người dùng chưa có đơn hàng nào
        if (Boolean.TRUE.equals(v.getForFirstOrder()) && user != null) {
            long orderCount = orderRepo.countByUser(user);
            if (orderCount > 0) {
                return new PromotionResult(false, "Mã chỉ dành cho đơn hàng đầu tiên.", BigDecimal.ZERO, subtotal.add(shipping));
            }
        }
        
        // Kiểm tra điều kiện forBirthday: user phải có ngày sinh và đang trong khoảng sinh nhật
        // TODO: Implement logic forBirthday khi User entity có trường birthday
        if (Boolean.TRUE.equals(v.getForBirthday())) {
            // Hiện tại User chưa có trường birthday, bỏ qua kiểm tra này
            // Có thể thêm sau khi User entity được cập nhật
        }
        
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

    private record SuggestProbeWithNote(String code, BigDecimal potential, String note, PromoRule rule) {}

    @Override
    public TwoCouponsResult applyBoth(String codeDiscount, String codeShip,
                                      BigDecimal subtotal, BigDecimal shipping, User user) {
        // discount
        var r1 = (codeDiscount == null || codeDiscount.isBlank())
                ? new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping))
                : applyVoucher(codeDiscount, subtotal, shipping, user);

        // ship
        var r2 = (codeShip == null || codeShip.isBlank())
                ? new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping))
                : applyVoucher(codeShip, subtotal, shipping, user);

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
        var now = LocalDateTime.now();
        var vouchers = voucherRepo.findActiveNow(now);

        // Phân loại voucher dựa vào ruleJson
        var discountList = new java.util.ArrayList<PromotionSuggestion>();
        var shipList = new java.util.ArrayList<PromotionSuggestion>();

        for (Voucher v : vouchers) {
            var rule = readRuleSafe(v.getRuleJson());
            
            // Kiểm tra minTotal
            if (rule.getMinTotal() != null && subtotal.compareTo(rule.getMinTotal()) < 0) {
                continue;
            }
            
            String note = buildVoucherNote(rule);
            var suggestion = new PromotionSuggestion(v.getCode(), v.getCode(), note);
            
            // Phân loại: Nếu là freeship thì vào shipList, còn lại vào discountList
            if (Boolean.TRUE.equals(rule.getFreeShip())) {
                shipList.add(suggestion);
            } else {
                // Voucher giảm giá/% phải có discount > 0 mới hiển thị
                var discount = potentialDiscount(v, subtotal, shipping);
                
                if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                discountList.add(suggestion);
            }
        }

        // Sắp xếp discount list theo giá trị giảm giá giảm dần
        discountList.sort((a, b) -> {
            var voucherA = vouchers.stream().filter(v -> v.getCode().equals(a.code())).findFirst();
            var voucherB = vouchers.stream().filter(v -> v.getCode().equals(b.code())).findFirst();
            if (voucherA.isEmpty() || voucherB.isEmpty()) return 0;
            var discA = potentialDiscount(voucherA.get(), subtotal, shipping);
            var discB = potentialDiscount(voucherB.get(), subtotal, shipping);
            return discB.compareTo(discA);
        });
        
        // Sắp xếp ship list theo minTotal tăng dần (ưu tiên voucher dễ dùng nhất trước)
        shipList.sort((a, b) -> {
            var voucherA = vouchers.stream().filter(v -> v.getCode().equals(a.code())).findFirst();
            var voucherB = vouchers.stream().filter(v -> v.getCode().equals(b.code())).findFirst();
            if (voucherA.isEmpty() || voucherB.isEmpty()) return 0;
            var ruleA = readRuleSafe(voucherA.get().getRuleJson());
            var ruleB = readRuleSafe(voucherB.get().getRuleJson());
            var minA = ruleA.getMinTotal() != null ? ruleA.getMinTotal() : BigDecimal.ZERO;
            var minB = ruleB.getMinTotal() != null ? ruleB.getMinTotal() : BigDecimal.ZERO;
            return minA.compareTo(minB); // minTotal nhỏ hơn (dễ dùng hơn) lên trước
        });

        // Giới hạn 10 voucher mỗi loại
        var topDiscount = discountList.stream().limit(10).toList();
        var topShip = shipList.stream().limit(10).toList();
        
        return new SuggestionsPair(topDiscount, topShip);
    }

    @Override
    public void incrementVoucherUsage(String code) {
        if (code == null || code.isBlank()) {
            return;
        }

        var opt = voucherRepo.findByCodeActiveNow(code.trim(), LocalDateTime.now());
        if (opt.isEmpty()) {
            // Voucher không tồn tại hoặc không active, bỏ qua
            return;
        }

        var voucher = opt.get();
        
        // Tăng usedCount lên 1
        Integer currentCount = voucher.getUsedCount();
        if (currentCount == null) {
            currentCount = 0;
        }
        voucher.setUsedCount(currentCount + 1);

        // Kiểm tra xem có đạt usageLimit chưa, nếu có thì chuyển status sang EXHAUSTED
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            voucher.setStatus("EXHAUSTED");
        }

        voucherRepo.save(voucher);
    }

    @Override
    @Deprecated
    public PromotionResult findBestAutoPromotion(BigDecimal subtotal, BigDecimal shipping, User user) {
        // Giữ lại để tương thích, nhưng khuyến khích dùng findBestAutoPromotions()
        var result = findBestAutoPromotions(subtotal, shipping, user);
        var totalDiscount = result.totalDiscount();
        var total = subtotal.add(shipping).subtract(totalDiscount).max(BigDecimal.ZERO);
        
        if (!result.hasAny()) {
            return new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping));
        }
        
        return new PromotionResult(true, result.displayMessage(), totalDiscount, total);
    }

    @Override
    public AutoPromotionResult findBestAutoPromotions(BigDecimal subtotal, BigDecimal shipping, User user) {
        var now = LocalDateTime.now();
        var promotions = promotionRepo.findActiveNow(now);
        
        if (promotions.isEmpty()) {
            return new AutoPromotionResult(
                    new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping)),
                    new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping))
            );
        }

        // Tách promotions theo loại
        Promotion bestDiscount = null;
        BigDecimal bestDiscountValue = BigDecimal.ZERO;
        
        Promotion bestFreeship = null;
        BigDecimal bestFreeshipValue = BigDecimal.ZERO;

        for (Promotion promo : promotions) {
            var rule = readRuleSafe(promo.getRuleJson());
            
            // Kiểm tra minTotal
            if (rule.getMinTotal() != null && subtotal.compareTo(rule.getMinTotal()) < 0) {
                continue;
            }

            // Phân loại theo type
            if (promo.getType() == PromoType.FREESHIP) {
                // Freeship: Chọn dựa vào minTotal thấp nhất (dễ dùng nhất), không cần so sánh discount
                var discount = computeDiscountByRule(rule, subtotal, shipping, promo.getType());
                
                if (bestFreeship == null) {
                    // Chưa có freeship nào, chọn cái này
                    bestFreeship = promo;
                    bestFreeshipValue = discount;
                } else {
                    // Đã có freeship, so sánh minTotal (ưu tiên minTotal thấp hơn)
                    var currentMinTotal = rule.getMinTotal() != null ? rule.getMinTotal() : BigDecimal.ZERO;
                    var bestMinTotal = readRuleSafe(bestFreeship.getRuleJson()).getMinTotal();
                    bestMinTotal = bestMinTotal != null ? bestMinTotal : BigDecimal.ZERO;
                    
                    if (currentMinTotal.compareTo(bestMinTotal) < 0) {
                        // minTotal thấp hơn → dễ dùng hơn → chọn cái này
                        bestFreeship = promo;
                        bestFreeshipValue = discount;
                    }
                }
            } else {
                // PERCENT hoặc AMOUNT: So sánh discount value
                var discount = computeDiscountByRule(rule, subtotal, shipping, promo.getType());
                
                if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                if (discount.compareTo(bestDiscountValue) > 0) {
                    bestDiscountValue = discount;
                    bestDiscount = promo;
                }
            }
        }

        // Build result cho discount promotion
        PromotionResult discountResult;
        if (bestDiscount != null) {
            String msg = bestDiscount.getTitle() != null && !bestDiscount.getTitle().isBlank()
                    ? bestDiscount.getTitle()
                    : "Giảm giá tự động";
            var total = subtotal.add(shipping).subtract(bestDiscountValue).max(BigDecimal.ZERO);
            discountResult = new PromotionResult(true, msg, bestDiscountValue, total);
        } else {
            discountResult = new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping));
        }

        // Build result cho freeship promotion
        PromotionResult freeshipResult;
        if (bestFreeship != null) {
            String msg = bestFreeship.getTitle() != null && !bestFreeship.getTitle().isBlank()
                    ? bestFreeship.getTitle()
                    : "Miễn phí ship";
            var total = subtotal.add(shipping).subtract(bestFreeshipValue).max(BigDecimal.ZERO);
            freeshipResult = new PromotionResult(true, msg, bestFreeshipValue, total);
        } else {
            freeshipResult = new PromotionResult(false, null, BigDecimal.ZERO, subtotal.add(shipping));
        }

        return new AutoPromotionResult(discountResult, freeshipResult);
    }
}

