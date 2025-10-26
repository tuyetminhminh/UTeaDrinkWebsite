package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    private Long shopId;
    private String shopName;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    // Summary
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer canceledOrders;
    private Double cancelRate;
    private BigDecimal averageOrderValue;
    
    // Top products
    private List<TopProductDTO> topProducts;
    
    // Revenue by day
    private List<DailyRevenueDTO> dailyRevenue;
    
    // Revenue by hour
    private List<HourlyRevenueDTO> hourlyRevenue;
}

