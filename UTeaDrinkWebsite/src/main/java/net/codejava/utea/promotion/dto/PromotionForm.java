package net.codejava.utea.promotion.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionForm {
    private Long id;

    @NotNull
    private PromoScope scope = PromoScope.GLOBAL;

    // Sẽ được validate trong service nếu scope là SHOP
    private Long shopId;

    @NotNull
    private PromoType type = PromoType.PERCENT;

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 500)
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime activeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime activeTo;

    private String status = "ACTIVE";

    // === CÁC TRƯỜNG QUY TẮC (FLATTENED FROM ruleJson) ===

    // Điều kiện chung
    private BigDecimal minTotal; // Giá trị đơn hàng tối thiểu
    private Boolean onlyNewUser;

    // Quy tắc cho loại PERCENT
    @Min(0) @Max(100)
    private Integer percentOff; // % giảm
    private BigDecimal amountCap; // Giảm tối đa

    // Quy tắc cho loại AMOUNT (giảm tiền trực tiếp)
    private BigDecimal amountOff;

    // Quy tắc cho loại SHIPPING
    private BigDecimal shipDiscountAmount; // Số tiền giảm phí ship
    private Boolean isFreeShip;

    // Áp dụng cho danh mục/sản phẩm cụ thể
    private List<Long> categoryIds;
    private List<Long> productIds;
}