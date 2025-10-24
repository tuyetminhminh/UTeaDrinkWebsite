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
public class HourlyRevenueDTO {
    private Integer hour; // 0-23
    private BigDecimal revenue;
    private Integer orderCount;
}

