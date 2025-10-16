package net.codejava.utea.customer.dto;

import net.codejava.utea.entity.Customer;

public record AddToCartRequest(
        Customer customer,
        Long productId,
        int quantity,
        Long sizeId   // null nếu sp không có size
) {}
