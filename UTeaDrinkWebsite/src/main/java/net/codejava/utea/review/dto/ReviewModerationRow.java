package net.codejava.utea.review.dto;

import net.codejava.utea.review.entity.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewModerationRow(
    Long id,
    String productName,
    String userName,
    Integer rating,
    String content,
    ReviewStatus status,
    LocalDateTime createdAt
) {}