// net/codejava/utea/common/service/CustomUserDetailsService.java
package net.codejava.utea.common.service;

import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Service tải thông tin người dùng cho Spring Security.
 * - loadUserByUsername(..) nhận email hoặc username.
 * - Bổ sung loadUserByEmail / loadUserById để chỗ khác dùng.
 */
public interface CustomUserDetailsService extends UserDetailsService {

    /** Covariant return type: trả CustomUserDetails luôn cho tiện dùng */
    @Override
    CustomUserDetails loadUserByUsername(String principal) throws UsernameNotFoundException;

    CustomUserDetails loadUserByEmail(String email) throws UsernameNotFoundException;

    CustomUserDetails loadUserById(Long id) throws UsernameNotFoundException;

    /** Helper cho WS: principal.getName() có thể là email hoặc username */
    default CustomUserDetails loadByPrincipalName(String name) throws UsernameNotFoundException {
        return loadUserByUsername(name);
    }
}
