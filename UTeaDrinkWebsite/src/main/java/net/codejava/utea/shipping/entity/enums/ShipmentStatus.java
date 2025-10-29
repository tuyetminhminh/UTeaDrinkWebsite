package net.codejava.utea.shipping.entity.enums;

public enum ShipmentStatus {

	ASSIGNED,     // đã gán shipper
    PICKED,       // đã lấy hàng tại shop
    DELIVERING,   // đang giao
    DELIVERED,    // giao thành công
    FAILED,       // giao thất bại (khách không nhận/không liên lạc được…)
    CANCELED
}
