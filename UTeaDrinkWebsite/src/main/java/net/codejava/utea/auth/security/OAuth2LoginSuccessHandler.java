package net.codejava.utea.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.cookie-name:UTEA_TOKEN}")
    private String cookieName;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-samesite:Lax}")
    private String sameSite;

    @Value("${security.jwt.expiration-time:604800000}") // 7 days
    private long expMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // OAuth2User sau khi login
        var principal = authentication.getPrincipal();

        // ⚠️ Phải lấy email từ attributes (KHÔNG dùng getName(), vì thường là "sub")
        String email = null;
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oau) {
            Object v = oau.getAttributes().get("email");
            if (v instanceof String s && !s.isBlank()) {
                email = s;
            }
        }

        if (email == null) {
            response.sendError(500, "OAuth2 login không trả về email. Hãy bật scope 'email' & 'profile' trong Google Client.");
            return;
        }

        // Load lại user từ DB bằng email để lấy quyền chuẩn
        var userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        String roleSummary = userDetails.getRoleSummary(); // ví dụ: "CUSTOMER" hoặc "CUSTOMER,MANAGER"

        // Sinh JWT với SUBJECT = EMAIL (rất quan trọng để JwtAuthFilter tải lại đúng user)
        String token = jwtService.generateToken(email, roleSummary, expMs);

        // Set cookie JWT
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(expMs / 1000)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect theo role
        Set<String> roles = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        String target = "/customer/home";
        if (roles.contains("ADMIN")) {
            target = "/admin/home";
        } else if (roles.contains("MANAGER")) {
            target = "/manager/home";
        } else if (roles.contains("SHIPPER")) {
            target = "/shipper/home";
        }
        response.sendRedirect(target);
    }
}
