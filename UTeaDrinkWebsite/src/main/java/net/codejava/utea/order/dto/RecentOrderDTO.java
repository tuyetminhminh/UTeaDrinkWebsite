package net.codejava.utea.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrderDTO(
    String orderCode,
    String customerName,
    LocalDateTime createdAt,
    BigDecimal total
) {}