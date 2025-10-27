package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

	Page<ProductCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	Optional<ProductCategory> findByName(String name);
}
