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
public class ShopDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String status; // OPEN, CLOSED, MAINTENANCE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Manager info
    private Long managerId;
    private String managerName;
}

