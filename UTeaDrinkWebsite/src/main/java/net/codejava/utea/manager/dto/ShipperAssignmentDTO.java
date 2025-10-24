package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperAssignmentDTO {
    private Long id;
    private Long orderId;
    private String orderCode;
    private Long shipperId;
    private String shipperName;
    private String shipperPhone;
    private String vehicleType;
    private String status; // ASSIGNED, PICKED_UP, DELIVERING, DELIVERED, FAILED
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String note;
}

