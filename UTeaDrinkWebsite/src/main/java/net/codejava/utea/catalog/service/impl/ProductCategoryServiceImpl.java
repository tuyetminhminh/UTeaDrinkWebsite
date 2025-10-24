package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.service.ProductCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

	private final ProductCategoryRepository repo;

	@Override
	public ProductCategory save(ProductCategory entity) {
		return repo.save(entity);
	}

	@Override
	public List<ProductCategory> findAll() {
		return repo.findAll();
	}

	@Override
	public Page<ProductCategory> findAll(Pageable pageable) {
		return repo.findAll(pageable);
	}

	@Override
	public Optional<ProductCategory> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		repo.deleteById(id);
	}

	@Override
	public Page<ProductCategory> search(String name, Pageable pageable) {
		if (name == null || name.isBlank()) {
			return repo.findAll(pageable);
		}
		return repo.findByNameContainingIgnoreCase(name.trim(), pageable);
	}

	@Override
	public boolean nameExists(String name, Long excludeId) {
		if (name == null || name.isBlank())
			return false;
		String n = name.trim();
		return (excludeId == null) ? repo.existsByNameIgnoreCase(n) : repo.existsByNameIgnoreCaseAndIdNot(n, excludeId);
	}
}
