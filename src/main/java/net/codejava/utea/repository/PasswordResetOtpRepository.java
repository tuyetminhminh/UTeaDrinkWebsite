package net.codejava.utea.repository;

import net.codejava.utea.entity.Account;
import net.codejava.utea.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByAccountAndCodeAndUsedIsFalseAndExpiresAtAfterOrderByIdDesc(
            Account account, String code, LocalDateTime now);
}
