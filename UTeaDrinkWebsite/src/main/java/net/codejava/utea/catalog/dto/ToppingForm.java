package net.codejava.utea.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ToppingForm {
    private Long id;

    @NotNull(message = "Vui lòng chọn cửa hàng.")
    private Long shopId;

    @NotBlank @Size(max = 150)
    private String name;

    @NotNull @DecimalMin(value = "0.0", inclusive = true, message = "Giá ≥ 0")
    private BigDecimal price;

    @NotBlank
    private String status = "ACTIVE"; // ACTIVE | INACTIVE
}