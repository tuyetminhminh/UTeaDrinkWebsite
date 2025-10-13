package net.codejava.utea.service.impl;

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
import java.util.HashMap;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secret;

    @Value("${security.jwt.expiration-time:3600000}")
    private long expirationMs;

    // ✅ Tạo SecretKey từ chuỗi secret
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ Sinh token có thêm claim "role"
    public String generateToken(UserDetails user, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // 🔥 thay vì authorities

        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ✅ Trích xuất username (subject)
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ✅ Trích xuất role từ token (nếu cần)
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // ✅ Kiểm tra token hợp lệ
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
