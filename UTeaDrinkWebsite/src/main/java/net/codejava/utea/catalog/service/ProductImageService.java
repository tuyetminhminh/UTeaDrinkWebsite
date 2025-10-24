
package net.codejava.utea.catalog.service;

import net.codejava.utea.catalog.entity.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageService {

	List<ProductImage> findByProduct(Long productId);

	Optional<ProductImage> findById(Long id);

	ProductImage save(ProductImage image);

	List<ProductImage> saveAll(List<ProductImage> images);

	void deleteById(Long id);;
}
