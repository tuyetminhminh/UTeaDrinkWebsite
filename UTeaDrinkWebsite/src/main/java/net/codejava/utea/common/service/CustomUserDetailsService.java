package net.codejava.utea.common.service;

import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Service tải thông tin người dùng cho Spring Security.
 * - loadUserByUsername(...) sẽ nhận email (vì hệ thống đăng nhập bằng email).
 * - Bổ sung tiện ích loadUserByEmail / loadUserById để chỗ khác dùng.
 */
public interface CustomUserDetailsService extends UserDetailsService {

    CustomUserDetails loadUserByEmail(String email);

    CustomUserDetails loadUserById(Long id);
}
