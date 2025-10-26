package net.codejava.utea.common.repository;

import net.codejava.utea.common.entity.PasswordResetOtp;
import net.codejava.utea.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
	Optional<PasswordResetOtp> findTopByUserAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(User user,
			LocalDateTime now);
}