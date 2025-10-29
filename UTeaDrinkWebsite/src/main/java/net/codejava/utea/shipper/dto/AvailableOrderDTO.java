package net.codejava.utea.shipper.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AvailableOrderDTO {
    private Long orderId;
    private String orderCode;
    
    // Shop info
    private Long shopId;
    private String shopName;
    private String shopAddress;
    
    // Customer info
    private String customerName;
    private String customerPhone;
    
    // Delivery address
    private String deliveryAddress;
    private String district;
    private String ward;
    
    // Order info
    private Integer itemCount;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private BigDecimal collectAmount; // Tiền thu hộ (= 0 nếu đã thanh toán online)
    private String paymentMethod;
    private String orderStatus; // NEW, PAID, CONFIRMED, etc.
    
    // Time
    private LocalDateTime createdAt;
    private String timeAgo; // "2 giờ trước"
    
    // Distance (optional - có thể tính sau)
    private String estimatedDistance;
}

