package net.codejava.utea.auth.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import net.codejava.utea.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JwtServiceImpl for JJWT 0.12.x
 * - Accepts plain-text or Base64/Base64Url secret (>= 32 bytes)
 * - HS256 signing
 */
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.secret-key:CHANGE_ME_TO_A_LONG_RANDOM_256bit_SECRET}")
    private String secretProp;

    private SecretKey key;

    @PostConstruct
    void initKey() {
        // Ưu tiên giải mã Base64/Url; nếu lỗi thì dùng bytes của chuỗi
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secretProp);
        } catch (Exception e1) {
            try {
                raw = Decoders.BASE64URL.decode(secretProp);
            } catch (Exception e2) {
                raw = secretProp.getBytes(StandardCharsets.UTF_8);
            }
        }
        // Yêu cầu tối thiểu 256-bit (32 bytes) cho HS256
        if (raw.length < 32) {
            throw new IllegalStateException("security.jwt.secret-key must be at least 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(raw);
    }

    @Override
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String sub = claims.getSubject();
            Date exp = claims.getExpiration();
            boolean notExpired = (exp == null) || exp.after(new Date());
            return sub != null && sub.equalsIgnoreCase(userDetails.getUsername()) && notExpired;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateToken(String subject, Map<String, Object> extraClaims, long ttlMillis) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(now));
        if (ttlMillis > 0) {
            builder.expiration(new Date(now + ttlMillis));
        }
        // JJWT 0.12.x: nên chỉ rõ thuật toán
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }
}
