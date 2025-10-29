package net.codejava.utea.customer.dto;

import net.codejava.utea.common.entity.User;

public record AddToCartRequest(
        User customer,
        Long productId,
        int quantity,
        Long variantId // null nếu sp không có size
) {}