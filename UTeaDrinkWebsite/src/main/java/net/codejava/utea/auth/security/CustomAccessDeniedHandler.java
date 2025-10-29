package net.codejava.utea.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý khi người dùng ĐÃ đăng nhập nhưng KHÔNG CÓ QUYỀN truy cập (403 Forbidden)
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        // Kiểm tra xem request có phải là API không
        String requestURI = request.getRequestURI();
        boolean isApiRequest = requestURI.startsWith("/api/");
        
        if (isApiRequest) {
            // Với API request: trả về JSON
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"Access Denied\",\"message\":\"Bạn không có quyền truy cập tài nguyên này.\"}"
            );
        } else {
            // Với web request: redirect đến trang lỗi 403
            response.sendRedirect("/error/403");
        }
    }
}

