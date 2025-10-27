package net.codejava.utea.promotion.service;

import java.util.List;

public record SuggestionsPair(
        java.util.List<PromotionSuggestion> discountVouchers,
        java.util.List<PromotionSuggestion> shipVouchers
) {}