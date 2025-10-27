package net.codejava.utea.review.view;

import java.time.LocalDateTime;

public record ReviewView(Long id, String userName, Integer rating, String content, LocalDateTime createdAt) {}
