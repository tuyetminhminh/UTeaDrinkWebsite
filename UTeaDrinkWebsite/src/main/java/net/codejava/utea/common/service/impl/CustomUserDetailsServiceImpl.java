package net.codejava.utea.common.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

	private final UserRepository userRepository;

	@Override
    public CustomUserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        // nhận cả email hoặc username
        System.out.println("DEBUG: Tìm kiếm user với principal: " + principal);
        
        try {
            // Thử tìm theo username trước
            if (!principal.contains("@")) {
                System.out.println("DEBUG: Thử tìm theo username: " + principal);
                Optional<User> byUsername = userRepository.findByUsername(principal);
                if (byUsername.isPresent()) {
                    User u = byUsername.get();
                    System.out.println("DEBUG: Tìm thấy user theo username: " + u.getEmail() + " với roles: " + u.getRoles());
                    return new CustomUserDetails(u);
                }
            }
            
            // Thử tìm theo email
            System.out.println("DEBUG: Thử tìm theo email: " + principal);
            Optional<User> byEmail = userRepository.findByEmail(principal);
            if (byEmail.isPresent()) {
                User u = byEmail.get();
                System.out.println("DEBUG: Tìm thấy user theo email: " + u.getEmail() + " với roles: " + u.getRoles());
                return new CustomUserDetails(u);
            }
            
            // Nếu không tìm thấy
            System.out.println("DEBUG: Không tìm thấy user với principal: " + principal);
            throw new UsernameNotFoundException("Không tìm thấy user: " + principal);
            
        } catch (Exception e) {
            System.out.println("DEBUG: Lỗi khi tìm user: " + e.getMessage());
            throw new UsernameNotFoundException("Không tìm thấy user: " + principal);
        }
    }
	
	@Override
	public CustomUserDetails loadUserByEmail(String email) {
		User u = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
		return new CustomUserDetails(u);
	}

	@Override
	public CustomUserDetails loadUserById(Long id) {
		User u = userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user id=" + id));
		return new CustomUserDetails(u);
	}
}
