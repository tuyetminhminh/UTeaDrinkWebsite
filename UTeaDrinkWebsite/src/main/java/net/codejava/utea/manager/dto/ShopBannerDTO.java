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
public class ShopBannerDTO {
    private Long id;
    private Long shopId;
    private String title;
    private String imageUrl;
    private String link;
    private Integer sortOrder;
    
    @JsonProperty("isActive")
    private boolean active;
    
    private LocalDateTime createdAt;
    
    // Convenience methods for backward compatibility
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}

