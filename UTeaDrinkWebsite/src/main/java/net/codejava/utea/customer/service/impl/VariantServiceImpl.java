package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.customer.service.VariantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final ProductVariantRepository variantRepo;

    @Override
    public List<ProductVariant> findActiveByProduct(Long productId) {
        // Nếu có cột status thì lọc; hiện chưa có thì trả tất cả theo product
        return variantRepo.findByProduct_IdOrderByPriceAsc(productId);
    }
}
