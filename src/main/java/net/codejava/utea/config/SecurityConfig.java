package net.codejava.utea.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.codejava.utea.security.JwtAuthFilter;
import net.codejava.utea.security.JwtAuthenticationEntryPoint;
import net.codejava.utea.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          JwtAuthenticationEntryPoint entryPoint,
                          CustomUserDetailsService uds) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.entryPoint = entryPoint;
        this.userDetailsService = uds;
    }

    /* ==========================================================
       🔐 MÃ HÓA MẬT KHẨU
       ========================================================== */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // ⚠️ Dành cho DEV TEST
        // return new BCryptPasswordEncoder();   // ✅ Dùng khi đã mã hóa trong DB
    }

    /* ==========================================================
       👤 PROVIDER XÁC THỰC NGƯỜI DÙNG
       ========================================================== */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /* ==========================================================
       ⚙️ AUTH MANAGER
       ========================================================== */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /* ==========================================================
       🧭 HANDLER: CHUYỂN HƯỚNG THEO ROLE
       ========================================================== */
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/";

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                redirectUrl = "/admin/home";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("CUSTOMER"))) {
                redirectUrl = "/customer/home";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("SELLER"))) {
                redirectUrl = "/seller/home";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("SHIPPER"))) {
                redirectUrl = "/shipper/home";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    /* ==========================================================
       🧱 CHUỖI BẢO MẬT CHÍNH - CHỈ DÙNG JWT (STATELESS)
       ========================================================== */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))

                // 🚫 Không tạo session - chỉ dùng JWT
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // ✅ API công khai
                        .requestMatchers("/api/auth/**", "/login", "/register", "/forgot", "/reset", "/otp/**").permitAll()

                        // ✅ Public area (ai cũng truy cập được)
                        .requestMatchers("/", "/main-home", "/home", "/index", "/about", "/contact",
                                "/products", "/products/**", "/GuestProducts", "/GuestProducts/**",
                                "/fragments/**", "/css/**", "/js/**", "/images/**",
                                "/uploads/**", "/assets/**", "/ws/**"
                        ).permitAll()

                        // ✅ Phân quyền theo vai trò
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/seller/**").hasAuthority("SELLER")
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER")

                        // 🔒 Còn lại phải xác thực
                        .anyRequest().authenticated()
                )

                // 🚫 Tắt hoàn toàn form login và logout của session-based
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        // ✅ Thêm JWT filter trước filter chuẩn của Spring
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // ✅ Gắn provider xác thực
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
