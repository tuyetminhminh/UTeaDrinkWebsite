package net.codejava.utea.order.entity.enums;

public enum OrderStatus {
	NEW,           // khách vừa đặt
    CONFIRMED,     // shop xác nhận
    PREPARING,     // đang pha chế / chuẩn bị
    DELIVERING,    // đang giao
    DELIVERED,     // đã giao
    CANCELED,      // hủy
    RETURNED,      // trả hàng
    REFUNDED       // hoàn tiền
}