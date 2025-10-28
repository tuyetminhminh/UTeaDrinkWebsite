package net.codejava.utea.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

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