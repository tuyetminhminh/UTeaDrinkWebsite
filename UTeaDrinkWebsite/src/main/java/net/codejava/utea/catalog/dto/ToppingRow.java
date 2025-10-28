package net.codejava.utea.catalog.dto;

import java.math.BigDecimal;

public record ToppingRow(
    Long id, String name, BigDecimal price, String status,
    Long shopId, String shopName
) {}
