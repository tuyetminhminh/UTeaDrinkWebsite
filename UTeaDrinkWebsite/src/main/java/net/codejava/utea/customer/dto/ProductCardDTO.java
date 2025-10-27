package net.codejava.utea.customer.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductCardDTO(
        Long id,
        String name,
        BigDecimal basePrice,
        BigDecimal ratingAvg,
        Integer soldCount,
        List<ProductImageDTO> images  // để FE dùng product.images[0].ur
) {}
