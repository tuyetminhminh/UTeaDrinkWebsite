// net/codejava/utea/common/service/impl/CustomUserDetailsServiceImpl.java
package net.codejava.utea.common.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    /** Nhận email hoặc username */
    @Override
    public CustomUserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(principal)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + principal));

        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(principal); // để Spring hiển thị đúng "username" đã dùng khi đăng nhập
        // (Đừng log mật khẩu ra console)
        return cud;
    }

    @Override
    public CustomUserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(email);
        return cud;
    }

    @Override
    public CustomUserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user id=" + id));
        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(user.getUsername() != null ? user.getUsername() : user.getEmail());
        return cud;
    }
}
