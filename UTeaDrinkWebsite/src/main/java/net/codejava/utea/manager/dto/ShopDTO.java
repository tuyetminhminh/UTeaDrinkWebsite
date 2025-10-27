package net.codejava.utea.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO {
    private Long id;
    @NotBlank(message = "Tên cửa hàng là bắt buộc")
    private String name;
    @Size(max = 400)
    private String address;
    @Pattern(regexp = "^$|[0-9+(). \\-]{6,20}")
    private String phone;
    private String status; // OPEN, CLOSED, MAINTENANCE
    private Long managerId;    // << thêm
    private String managerName; // << thêm
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ShopDTO(Long id, String name, String address, String phone, String status,
               LocalDateTime createdAt, LocalDateTime updatedAt,
               Long managerId, String managerName) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.phone = phone;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.managerId = managerId;
    this.managerName = managerName;
}

}

