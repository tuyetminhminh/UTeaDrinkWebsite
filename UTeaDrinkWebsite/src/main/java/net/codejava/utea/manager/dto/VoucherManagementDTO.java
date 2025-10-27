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
public class VoucherManagementDTO {
    private Long id;
    private String code;
    private String scope; // GLOBAL, SHOP
    private Long shopId;
    private String shopName;
    private String ruleJson;
    private Boolean forFirstOrder;
    private Boolean forBirthday;
    private LocalDateTime activeFrom;
    private LocalDateTime activeTo;
    private String status; // ACTIVE, INACTIVE, EXHAUSTED
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean isEditable; // Manager có thể edit/delete voucher này không (false nếu GLOBAL)
}

