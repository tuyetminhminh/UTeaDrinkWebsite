package net.codejava.utea.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Value("${app.jwt.cookie-name:UTEA_TOKEN}")
    private String cookieName;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-samesite:Lax}")
    private String sameSite;

    @Value("${security.jwt.expiration-time:604800000}") // 7 ngày
    private long expMs;

    /** Đăng nhập JSON -> trả token và gắn cookie HttpOnly để dùng trên web */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String email = body.getOrDefault("email", body.get("username")); // cho phép client gửi "email" hoặc "username"
        String password = body.get("password");

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        String token = jwtService.generateTokenFromEmail(user.getUsername(), user.getRoleSummary());

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(expMs / 1000)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of(
                "token", token,
                "email", user.getUsername(),
                "displayName", user.getDisplayName(),
                "roles", user.getRoleSummary()
        );
    }

    /** Đăng xuất JSON: xoá cookie JWT */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Map.of("message", "OK");
    }
}
