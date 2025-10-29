package net.codejava.utea.common.repository;

import net.codejava.utea.common.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
    Optional<Role> findByCode(String code); // ADMIN, CUSTOMER, MANAGER, SHIPPER
    
    List<Role> findByCodeIn(Collection<String> codes);
}
