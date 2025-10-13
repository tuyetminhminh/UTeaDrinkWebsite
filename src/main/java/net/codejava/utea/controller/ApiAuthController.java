package net.codejava.utea.controller;

import jakarta.servlet.http.HttpServletResponse;
import net.codejava.utea.service.impl.JwtService;
import net.codejava.utea.service.impl.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public ApiAuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    /**
     * 🔐 Đăng nhập, tạo JWT và gửi về cả trong JSON + Cookie
     */
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        String username = body.get("username");
        String password = body.get("password");

        // ✅ Xác thực tài khoản
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        // ✅ Tạo JWT
        String token = jwtService.generateToken(user, user.getRole());

        // ✅ Gắn cookie chứa JWT để các request web (Thymeleaf) dùng được
        ResponseCookie cookie = ResponseCookie.from("UTEA_TOKEN", token)
                .httpOnly(true)           // Không cho JS truy cập
                .secure(false)            // Đặt true nếu deploy HTTPS
                .path("/")                // Dùng được toàn site
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .sameSite("Lax")          // Giúp hoạt động tốt khi redirect nội bộ
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ✅ Trả JSON để client app (Postman, mobile app, React, v.v.) vẫn dùng được
        return Map.of(
                "token", token,
                "role", user.getRole(),
                "username", user.getUsername()
        );
    }

    /**
     * 🚪 Đăng xuất - xóa cookie JWT
     */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("UTEA_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Map.of("message", "Đăng xuất thành công");
    }
}
