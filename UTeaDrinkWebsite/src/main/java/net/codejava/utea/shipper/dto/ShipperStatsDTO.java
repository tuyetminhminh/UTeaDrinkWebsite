package net.codejava.utea.shipper.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShipperStatsDTO {
    // Hôm nay
    private Integer todayOrders;
    private BigDecimal todayEarnings; // Tổng phí ship hôm nay
    
    // Tuần này
    private Integer weekOrders;
    private BigDecimal weekEarnings;
    
    // Tháng này
    private Integer monthOrders;
    private BigDecimal monthEarnings;
    
    // Tổng
    private Integer totalOrders;
    private BigDecimal totalEarnings;
    
    // Hiện tại
    private Integer deliveringCount; // Đang giao
    private Integer availableCount; // Đơn khả dụng
    
    // Thống kê
    private Double averageRating; // Đánh giá trung bình (nếu có)
    private Integer completedOrders; // Đơn đã giao
    private Integer canceledOrders; // Đơn bị hủy
}

