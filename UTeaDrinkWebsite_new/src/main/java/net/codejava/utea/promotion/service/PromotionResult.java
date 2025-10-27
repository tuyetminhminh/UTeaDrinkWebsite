package net.codejava.utea.promotion.service;

import java.math.BigDecimal;

public record PromotionResult(boolean ok, String message, BigDecimal discount, BigDecimal total) {}
