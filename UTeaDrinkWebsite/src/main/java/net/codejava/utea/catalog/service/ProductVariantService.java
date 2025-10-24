package net.codejava.utea.catalog.service;

import net.codejava.utea.catalog.entity.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    List<ProductVariant> findActiveByProduct(Long productId);
    ProductVariant cheapest(List<ProductVariant> variants);
}
