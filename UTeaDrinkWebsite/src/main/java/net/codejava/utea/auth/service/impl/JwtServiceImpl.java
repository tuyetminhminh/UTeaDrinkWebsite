package net.codejava.utea.auth.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	@Value("${security.jwt.secret-key:CHANGE_ME_TO_A_LONG_RANDOM_256bit_SECRET}")
	private String secret;

	private SecretKey key() {
		// jjwt 0.12.x
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String extractUsername(String token) {
		return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	@Override
	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			var payload = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
			String sub = payload.getSubject();
			Date exp = payload.getExpiration();
			return sub != null && sub.equalsIgnoreCase(userDetails.getUsername())
					&& (exp == null || exp.after(new Date()));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String generateToken(String subject, Map<String, Object> extraClaims, long ttlMillis) {
		long now = System.currentTimeMillis();
		var builder = Jwts.builder().subject(subject).issuedAt(new Date(now)).claims(extraClaims).signWith(key());

		if (ttlMillis > 0)
			builder.expiration(new Date(now + ttlMillis));
		return builder.compact();
	}
}
