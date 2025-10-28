package net.codejava.utea.admin.dto;

import lombok.Builder;
import net.codejava.utea.order.dto.RecentOrderDTO;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminDashboardStatsDTO(
    // Dữ liệu cho các thẻ KPI
    BigDecimal monthlyRevenue,
    long monthlyOrders,
    long newUsersThisMonth,
    long activeProducts,
    
    // Dữ liệu cho bảng "Đơn gần đây"
    List<RecentOrderDTO> recentOrders,
    
    // Dữ liệu cho biểu đồ
    List<String> chartLabels, // vd: ["21/10", "22/10", ...]
    List<BigDecimal> chartData // vd: [1500000, 2300000, ...]
) {}