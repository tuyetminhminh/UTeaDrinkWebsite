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
        // nhận cả email hoặc username
        User u = userRepository.findByEmailOrUsername(principal)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + principal));
        return new CustomUserDetails(u);
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
