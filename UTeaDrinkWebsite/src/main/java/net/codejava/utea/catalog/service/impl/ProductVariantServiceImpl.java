package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.catalog.service.ProductVariantService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepo;

    @Override
    public List<ProductVariant> findActiveByProduct(Long productId) {
        // Hiện tại entity chưa có field 'active' => trả về tất cả biến thể theo product, đã sort theo price
        return variantRepo.findByProduct_IdOrderByPriceAsc(productId);
    }

    @Override
    public ProductVariant cheapest(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return null;
        return variants.stream()
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElse(null);
    }
}
