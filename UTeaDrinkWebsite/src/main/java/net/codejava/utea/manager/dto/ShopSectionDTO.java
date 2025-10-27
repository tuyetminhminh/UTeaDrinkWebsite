package net.codejava.utea.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSectionDTO {
    private Long id;
    private Long shopId;
    private String title;
    private String sectionType; // FEATURED, TOP_SELLING, NEW_ARRIVALS, PROMOTION
    private String contentJson;
    private Integer sortOrder;
    
    @JsonProperty("isActive")
    private Boolean isActive; // Changed to Boolean wrapper for better JSON handling
    
    private LocalDateTime createdAt;
}

