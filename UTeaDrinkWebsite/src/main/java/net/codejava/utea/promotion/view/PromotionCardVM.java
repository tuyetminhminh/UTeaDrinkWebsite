package net.codejava.utea.promotion.view;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * View Model cho customer xem chương trình khuyến mãi tự động
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCardVM {
    private Long id;
    private String title;
    private String description;
    
    private String scope; // GLOBAL hoặc SHOP
    private String shopName;
    private String status;
    private String type; // PERCENT, AMOUNT, SHIPPING
    
    // Rule details
    private boolean freeShip;
    private Integer percentOff;
    private BigDecimal amountOff;
    private BigDecimal amountCap;
    private BigDecimal minTotal;
    private BigDecimal shipDiscountAmount;
    
    private LocalDateTime activeFrom;
    private LocalDateTime activeTo;
    
    private String typeText; // "Giảm %", "Giảm tiền", "Miễn phí ship"
}

