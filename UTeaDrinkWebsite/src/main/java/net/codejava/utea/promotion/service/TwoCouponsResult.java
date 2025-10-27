package net.codejava.utea.promotion.service;

import java.math.BigDecimal;

public record TwoCouponsResult(
        boolean okDiscount,
        String  msgDiscount,
        BigDecimal discount,     // giảm tiền/%
        boolean okShip,
        String  msgShip,
        BigDecimal shipDiscount, // giảm ship
        BigDecimal total
) {}