package net.codejava.utea.common.service;

import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;

import java.util.List;
import java.util.Optional;

public interface AddressService {
	Address save(Address a);

	void delete(Long id, Long ownerId);

	List<Address> listOf(User user);

	Optional<Address> findById(Long id);

	void markDefault(Long id, Long ownerId);
}
