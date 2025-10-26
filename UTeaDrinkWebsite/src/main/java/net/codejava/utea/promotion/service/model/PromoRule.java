package net.codejava.utea.promotion.service.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Khớp cấu trúc rule_json mô tả trong entity:
 * { "minTotal":100000, "percentOff":10, "amountOff":30000, "amountCap":50000,
 *   "freeShip":true, "onlyNewUser":false, "birthdayDaysOffset":7,
 *   "categoryIds":[1,2], "productIds":[10,11] }
 */
@Getter @Setter
public class PromoRule {
    private BigDecimal minTotal;       // ngưỡng tối thiểu
    private Integer percentOff;        // giảm theo %
    private BigDecimal amountOff;      // giảm số tiền cố định
    private BigDecimal amountCap;      // trần cho giảm theo %
    private Boolean freeShip;          // miễn/giảm ship (nếu true thì giảm tối đa = shippingFee)
    private Boolean onlyNewUser;       // (chưa dùng ở bản tối giản này)
    private Integer birthdayDaysOffset;// (chưa dùng ở bản tối giản này)
    private List<Long> categoryIds;    // (chưa lọc theo line item ở bản tối giản)
    private List<Long> productIds;     // (chưa lọc theo line item ở bản tối giản)
}