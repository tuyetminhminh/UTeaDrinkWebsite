package net.codejava.utea.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.utea.customer.entity.Size;

import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Long> {
    Optional<Size> findByCode(String code);
}