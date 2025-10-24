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
public class ShopBannerDTO {
    private Long id;
    private Long shopId;
    private String title;
    private String imageUrl;
    private String link;
    private Integer sortOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
}

