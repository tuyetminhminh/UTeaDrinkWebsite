package net.codejava.utea.customer.dto;

import java.util.List;

public record SectionDTO(
        String sectionType,   // FEATURED / NEW_ARRIVALS / TOP_SELLING / PROMOTION
        String title,
        List<ProductCardDTO> products
) {}
