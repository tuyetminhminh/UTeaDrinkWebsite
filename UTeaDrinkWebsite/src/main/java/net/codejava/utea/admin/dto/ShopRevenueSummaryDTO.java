package net.codejava.utea.admin.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ShopRevenueSummaryDTO(
    Long shopId,
    String shopName,
    BigDecimal totalRevenue,
    Integer totalOrders,
    BigDecimal averageOrderValue
) {}