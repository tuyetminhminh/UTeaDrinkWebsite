package net.codejava.utea.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Xử lý khi người dùng CHƯA đăng nhập nhưng cố truy cập tài nguyên yêu cầu authentication (401 Unauthorized)
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException ex) 
            throws IOException, ServletException {
        
        // Kiểm tra xem request có phải là API không
        String requestURI = request.getRequestURI();
        boolean isApiRequest = requestURI.startsWith("/api/");
        
        if (isApiRequest) {
            // Với API request: trả về JSON
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Bạn cần đăng nhập để truy cập tài nguyên này.\"}"
            );
        } else {
            // Với web request: redirect đến trang login
            response.sendRedirect("/login");
        }
    }
}
