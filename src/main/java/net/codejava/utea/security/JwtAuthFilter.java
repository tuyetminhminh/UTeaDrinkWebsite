package net.codejava.utea.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.codejava.utea.service.CustomUserDetailsService;
import net.codejava.utea.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.jwt.cookie-name:UTEA_TOKEN}")
    private String cookieName;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService uds) {
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> cookieName.equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtService.extractUsername(token);
                var user = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Token lỗi -> bỏ qua, để vào EntryPoint xử lý 401 khi endpoint yêu cầu auth.
            }
        }
        // **Xác thực WebSocket**: Kiểm tra nếu yêu cầu là WebSocket
        if (request.getRequestURI().startsWith("/ws")) {
            // Nếu là WebSocket, thực hiện xác thực
            String authHeaderWs = request.getHeader("Authorization");
            if (authHeaderWs != null && authHeaderWs.startsWith("Bearer ")) {
                String tokenWs = authHeaderWs.substring(7);
                // Kiểm tra token WebSocket
                if (jwtService.isTokenValid(tokenWs, userDetailsService.loadUserByUsername(jwtService.extractUsername(tokenWs)))) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Trả về Unauthorized nếu không hợp lệ
            return;
        }


        filterChain.doFilter(request, response);
    }
}
