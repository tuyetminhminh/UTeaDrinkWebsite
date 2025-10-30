package net.codejava.utea.promotion.dto;

import net.codejava.utea.promotion.entity.enums.PromoScope;

import java.time.LocalDateTime;

public record VoucherRow(
        Long id,
        String code,
        PromoScope scope,
        String shopName, // Tên shop, là '-' nếu là GLOBAL
        String status,
        LocalDateTime activeFrom,
        LocalDateTime activeTo,
        Integer usageLimit,
        Integer usedCount) {
}

