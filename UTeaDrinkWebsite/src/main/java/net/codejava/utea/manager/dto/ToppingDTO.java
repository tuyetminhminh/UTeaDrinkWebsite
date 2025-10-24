package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToppingDTO {
    private Long id;
    private Long shopId;
    private String name;
    private BigDecimal price;
    private String status; // ACTIVE, INACTIVE
}

