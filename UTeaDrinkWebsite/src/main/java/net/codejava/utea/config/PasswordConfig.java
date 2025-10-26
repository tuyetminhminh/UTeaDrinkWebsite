package net.codejava.utea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình PasswordEncoder tách riêng để tránh vòng phụ thuộc.
 * Dev: NoOp (DB đang plaintext). Prod: hãy chuyển sang BCryptPasswordEncoder().
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
        // return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(); // Prod
    }
}
