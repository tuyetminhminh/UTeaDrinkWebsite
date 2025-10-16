package net.codejava.utea.customer.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.entity.ProductVariant;
import net.codejava.utea.customer.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor
public class VariantService {
    private final ProductVariantRepository repo;

    public List<ProductVariant> findActiveByProduct(Long productId){
        return repo.findByProduct_ProductIdAndStatus(productId, "ACTIVE");
    }

    public ProductVariant get(Long id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Variant not found"));
    }

    public ProductVariant cheapest(List<ProductVariant> variants){
        return variants.stream()
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElseThrow(() -> new IllegalArgumentException("No variants"));
    }
}
