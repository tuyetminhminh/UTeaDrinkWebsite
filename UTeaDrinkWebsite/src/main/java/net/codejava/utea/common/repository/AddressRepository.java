package net.codejava.utea.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;

public interface AddressRepository extends JpaRepository<Address, Long>{

	List<Address> findByUserOrderByIsDefaultDescIdDesc(User user);

	List<Address> findByUserId(Long userId);
}
