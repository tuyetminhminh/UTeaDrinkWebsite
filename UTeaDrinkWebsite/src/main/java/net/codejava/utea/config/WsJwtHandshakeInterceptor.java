// net/codejava/utea/config/WsJwtHandshakeInterceptor.java
package net.codejava.utea.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsJwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService uds;

    @Value("${app.jwt.cookie-name:UTEA_TOKEN}")
    private String cookieName;

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (req instanceof ServletServerHttpRequest servlet) {
            HttpServletRequest http = servlet.getServletRequest();
            Cookie[] cookies = http.getCookies();
            if (cookies != null) {
                String token = Arrays.stream(cookies)
                        .filter(c -> cookieName.equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst().orElse(null);
                try {
                    if (token != null) {
                        String username = jwtService.extractUsername(token);
                        CustomUserDetails user = (CustomUserDetails) uds.loadUserByUsername(username);
                        if (jwtService.isTokenValid(token, user)) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities());
                            // gắn vào SecurityContext để Spring lấy Principal cho phiên WS
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return true;
    }

    @Override public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                         WebSocketHandler wsHandler, Exception ex) {}
}
