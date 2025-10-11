package net.codejava.utea.config;

import net.codejava.utea.service.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * ✅ Tạm thời không mã hóa mật khẩu (dùng NoOp cho dễ test)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
        // return new BCryptPasswordEncoder(); // Dùng khi đã hash mật khẩu trong DB
    }

    /**
     * ✅ Provider xác thực người dùng từ DB
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * ✅ Cấu hình bảo mật hệ thống
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để dễ test form
                .authorizeHttpRequests(auth -> auth

                        // ===== 🌐 CÁC TRANG KHÁCH XEM (KHÔNG CẦN LOGIN) =====
                        .requestMatchers(
                                "/",                     // root
                                "/main-home",
                                "/guest/**",// trang chủ chính
                                "/home",                 // alias
                                "/index",                // alias khác nếu có
                                "/about", "/contact",    // nếu có thêm menu
                                "/products", "/products/**",
                                "/fragments/**",         // header/footer fragments
                                "/css/**", "/js/**", "/images/**", "/uploads/**", "/assets/**"
                        ).permitAll()

                        // ===== 🔐 CÁC TRANG CÓ PHÂN QUYỀN =====
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/seller/**").hasAuthority("SELLER")
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER")
                        .requestMatchers("/chat/**").hasAnyAuthority("ADMIN", "CUSTOMER")

                        // ===== 📧 AUTH ROUTES =====
                        .requestMatchers("/login", "/register", "/forgot", "/reset", "/otp/**").permitAll()

                        // ===== 🚫 CÒN LẠI PHẢI ĐĂNG NHẬP =====
                        .anyRequest().authenticated()
                )

                // ===== 🔓 CẤU HÌNH LOGIN FORM =====
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
                            String role = user.getRole();

                            // ✅ Điều hướng sau đăng nhập
                            switch (role.toUpperCase()) {
                                case "ADMIN" -> response.sendRedirect("/admin/home");
                                case "CUSTOMER" -> response.sendRedirect("/customer/home");
                                case "SELLER" -> response.sendRedirect("/seller/home");
                                case "SHIPPER" -> response.sendRedirect("/shipper/home");
                                default -> response.sendRedirect("/main-home");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // ===== 🚪 LOGOUT =====
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/main-home?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ===== 🧱 BẢO VỆ FRAME / CLICKJACKING =====
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
