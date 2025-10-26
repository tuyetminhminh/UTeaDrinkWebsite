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
public class ReviewDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private Integer helpfulCount;
}

