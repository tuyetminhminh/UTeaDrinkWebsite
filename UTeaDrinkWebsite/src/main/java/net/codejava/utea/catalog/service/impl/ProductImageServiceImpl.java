package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductImage;
import net.codejava.utea.catalog.repository.ProductImageRepository;
import net.codejava.utea.catalog.service.ProductImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepo;

    @Override
    public List<ProductImage> findByProduct(Long productId) {
        return productImageRepo.findByProduct_Id(productId); // <-- đã đổi
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return productImageRepo.findById(id);
    }

    @Override
    @Transactional
    public ProductImage save(ProductImage image) {
        return productImageRepo.save(image);
    }

    @Override
    @Transactional
    public List<ProductImage> saveAll(List<ProductImage> images) {
        return productImageRepo.saveAll(images);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productImageRepo.deleteById(id);
    }
}
