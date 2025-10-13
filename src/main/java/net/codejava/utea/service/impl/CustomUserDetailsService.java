package net.codejava.utea.service.impl;

import net.codejava.utea.entity.Account;
import net.codejava.utea.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository repo;

    public CustomUserDetailsService(AccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Lấy Account từ DB
        Account acc = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        // Trả về CustomUserDetails (có thêm id, displayName, role)
        return new CustomUserDetails(acc);
    }
}
