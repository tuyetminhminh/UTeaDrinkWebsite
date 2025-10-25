package net.codejava.utea.manager.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToppingDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String status; // ACTIVE / HIDDEN
}
