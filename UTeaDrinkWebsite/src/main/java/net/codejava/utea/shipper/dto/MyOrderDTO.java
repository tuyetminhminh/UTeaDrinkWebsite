package net.codejava.utea.shipper.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MyOrderDTO {
    private Long orderId;
    private String orderCode;
    private String status; // DELIVERING, DELIVERED
    
    // Shop info
    private String shopName;
    private String shopPhone;
    private String shopAddress;
    
    // Customer info
    private String customerName;
    private String customerPhone;
    
    // Delivery address
    private String deliveryAddress;
    private String fullAddress; // Đầy đủ: ward, district, province
    
    // Order info
    private Integer itemCount;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private BigDecimal collectAmount; // Tiền thu hộ (= 0 nếu đã thanh toán online)
    private String orderStatus; // NEW, PAID, CONFIRMED, etc.
    
    // Assignment info
    private LocalDateTime assignedAt; // Thời điểm nhận đơn
    private LocalDateTime pickedUpAt; // Thời điểm lấy hàng
    private LocalDateTime deliveredAt; // Thời điểm giao hàng
    
    // Note & Proof
    private String deliveryNote;
    private String proofImageUrl;
    
    // Failed Delivery Info
    private String failureReason; // Lý do không giao được
    private String failureNote; // Ghi chú chi tiết
}

