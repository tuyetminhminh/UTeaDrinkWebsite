package net.codejava.utea.chat.dto;

import java.time.LocalDateTime;

public record UserModerationRow(
    Long id,
    String email,
    String fullName,
    LocalDateTime chatBannedUntil // null nếu không bị khóa
) {}