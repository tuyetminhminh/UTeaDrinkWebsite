package net.codejava.utea.promotion.dto;

import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;

import java.time.LocalDateTime;

public record PromotionRow(
        Long id,
        String title,
        PromoScope scope,
        String shopName, // Tên shop, là '-' nếu là GLOBAL
        PromoType type,
        String status,
        LocalDateTime activeFrom,
        LocalDateTime activeTo) {
}