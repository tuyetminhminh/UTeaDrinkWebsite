package net.codejava.utea.promotion.view;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VoucherCardVM {
    private Long id;
    private String code;

    private String scope;
    private String shopName;
    private String status;

    private boolean freeShip;
    private Integer percentOff;
    private BigDecimal amountOff;
    private BigDecimal amountCap;
    private BigDecimal minTotal;

    private LocalDateTime activeFrom;
    private LocalDateTime activeTo;

    private Integer usageLimit;
    private Integer usedCount;

    private String typeText;
    private boolean saved;
}
