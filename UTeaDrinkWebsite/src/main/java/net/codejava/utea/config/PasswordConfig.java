package net.codejava.utea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình PasswordEncoder với BCrypt để mã hóa mật khẩu an toàn.
 * BCrypt sử dụng hashing một chiều với salt ngẫu nhiên, không thể giải mã ngược.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ✅ SỬ DỤNG BCrypt - Chuẩn công nghiệp cho mã hóa password
        return new BCryptPasswordEncoder();
        
        // ❌ KHÔNG BAO GIỜ dùng NoOp trong production!
        // return NoOpPasswordEncoder.getInstance(); 
    }
}
