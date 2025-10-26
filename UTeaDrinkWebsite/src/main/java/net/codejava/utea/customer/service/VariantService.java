package net.codejava.utea.customer.service;

import net.codejava.utea.catalog.entity.ProductVariant;
import java.util.List;

public interface VariantService {
    List<ProductVariant> findActiveByProduct(Long productId);
}