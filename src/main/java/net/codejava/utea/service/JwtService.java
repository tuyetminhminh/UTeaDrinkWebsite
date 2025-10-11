package net.codejava.utea.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secret;

    @Value("${security.jwt.expiration-time:3600000}")
    private long expirationMs;

    // ✅ Trả về SecretKey thay vì Key
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails user, Map<String, Object> extra) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extra)
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                // ✅ Cú pháp mới, đúng cho 0.12.x:
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(UserDetails user, String role) {
        return generateToken(user, Map.of("authorities", role));
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        var payload = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String sub = payload.getSubject();
        Date exp = payload.getExpiration();
        return sub != null && sub.equals(user.getUsername()) && exp.after(new Date());
    }
}
