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
public class ProductManagementDTO {
    private Long id;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer soldCount;
    private BigDecimal ratingAvg;
    private String status; // AVAILABLE, HIDDEN, OUT_OF_STOCK
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Images
    private List<ProductImageDTO> images;
    
    // Variants
    private List<ProductVariantDTO> variants;
}

