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

    @Override
    public CustomUserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        // ✅ Tìm user theo email hoặc username (tự động nhận diện)
        User user = userRepository.findByEmailOrUsername(principal)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + principal));

        // ✅ Tạo CustomUserDetails và set lại loginId để Spring dùng đúng chuỗi xác thực
        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(principal);
        System.out.println(">>> [DEBUG] Đăng nhập bằng: " + principal);
        System.out.println(">>> [DEBUG] User trong DB: " + user.getUsername() + " / " + user.getEmail());
        System.out.println(">>> [DEBUG] Password trong DB: " + user.getPasswordHash());
        return cud;
    }

    @Override
    public CustomUserDetails loadUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(email);
        return cud;
    }

    @Override
    public CustomUserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user id=" + id));
        CustomUserDetails cud = new CustomUserDetails(user);
        cud.setLoginId(user.getUsername() != null ? user.getUsername() : user.getEmail());
        return cud;
    }
}
