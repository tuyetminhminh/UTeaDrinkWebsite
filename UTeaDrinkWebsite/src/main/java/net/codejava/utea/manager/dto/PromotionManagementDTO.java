package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionManagementDTO {
    private Long id;
    private String scope; // GLOBAL, SHOP
    private Long shopId;
    private String shopName;
    private String type; // PERCENT, AMOUNT, FREESHIP
    private String ruleJson;
    private String title;
    private String description;
    private LocalDateTime activeFrom;
    private LocalDateTime activeTo;
    private String status; // ACTIVE, INACTIVE
    private Boolean isEditable; // Manager có thể edit/delete promotion này không (false nếu GLOBAL)
}

