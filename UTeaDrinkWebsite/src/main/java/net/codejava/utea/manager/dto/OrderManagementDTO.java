package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderManagementDTO {
    private Long id;
    private String orderCode;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private Long shopId;
    private String shopName;
    private String status; // NEW, CONFIRMED, PREPARING, DELIVERING, DELIVERED, CANCELED, RETURNED, REFUNDED

    // Address
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    private BigDecimal total;

    private String voucherCode;
    private String paymentMethod;
    private String paymentStatus;

    // Items
    private List<OrderItemDTO> items;
    private Integer itemCount; // Tổng số món

    // Shipper
    private Long shipperId;
    private String shipperName;
    private String deliveryNote; // Ghi chú giao hàng
    private String proofImageUrl; // Ảnh bằng chứng giao hàng

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

