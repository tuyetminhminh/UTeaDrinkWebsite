package net.codejava.utea.order.entity.enums;

public enum OrderStatus {
	NEW,           // khách vừa đặt
    PAID,          // đã thanh toán online thành công đơn về 0 đồng
    CONFIRMED,     // shop xác nhận
    PREPARING,     // đang pha chế / chuẩn bị
    DELIVERING,    // đang giao
    DELIVERED,     // đã giao
    CANCELED,      // hủy
    RETURNED,      // trả hàng
    REFUNDED       // hoàn tiền
}