package net.codejava.utea.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ReviewCardDTO {
    private String productName;
    private String content;
}
