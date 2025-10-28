package net.codejava.utea.admin.dto;

import lombok.Builder;
import net.codejava.utea.manager.dto.DailyRevenueDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record SystemRevenueReportDTO(
    LocalDate fromDate,
    LocalDate toDate,

    // Dữ liệu tổng hợp toàn hệ thống
    BigDecimal totalSystemRevenue,
    Integer totalSystemOrders,
    Integer totalSystemCompletedOrders,
    Double systemCancelRate,
    BigDecimal systemAverageOrderValue,

    // Dữ liệu chi tiết cho từng cửa hàng
    List<ShopRevenueSummaryDTO> shopSummaries,

    // Dữ liệu tổng hợp theo ngày (để vẽ biểu đồ)
    List<DailyRevenueDTO> dailyRevenue
) {}