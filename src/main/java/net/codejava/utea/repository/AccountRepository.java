package net.codejava.utea.repository;

import net.codejava.utea.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    List<Account> findByRole(String role);
    Optional<Account> findFirstByRole(String role);
}
