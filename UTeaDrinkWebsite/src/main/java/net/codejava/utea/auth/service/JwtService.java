package net.codejava.utea.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

public interface JwtService {
	String extractUsername(String token);

	boolean isTokenValid(String token, UserDetails userDetails);

	// sinh token (subject = username/email) + extra claims (vd: role)
	String generateToken(String subject, Map<String, Object> extraClaims, long ttlMillis);

	// helper thường dùng
	default String generateToken(String subject, String role, long ttlMillis) {
		return generateToken(subject, Map.of("role", role), ttlMillis);
	}

	// backward-compatible theo code bạn đang gọi
	default String generateTokenFromEmail(String email, String role) {
		// TTL mặc định 7 ngày
		return generateToken(email, Map.of("role", role), 7 * 24 * 60 * 60 * 1000L);
	}
}
