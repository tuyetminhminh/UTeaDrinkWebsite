package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.security.JwtAuthFilter;
import net.codejava.utea.auth.security.JwtAuthenticationEntryPoint;
import net.codejava.utea.auth.security.OAuth2LoginSuccessHandler;
import net.codejava.utea.auth.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final UserDetailsService userDetailsService;

    // Nếu bạn có CustomOAuth2UserService riêng thì giữ, còn không có thể bỏ
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserServiceImpl;

    /**
     * ✅ Không mã hóa mật khẩu (so sánh chuỗi thuần)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();
        System.out.println(">>> [DEBUG] PasswordEncoder đang được dùng: " + encoder.getClass().getName());
        return encoder;
    }

    /**
     * ✅ Provider xử lý xác thực user/password
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * ✅ Cung cấp AuthenticationManager cho controller
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ Cấu hình Spring Security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì dùng JWT stateless
                .csrf(csrf -> csrf.disable())
                // Khi lỗi auth → dùng entryPoint
                .exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
                // Stateless session (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/main-home", "/error", "/error/**",
                                "/api/auth/**", "/login", "/register", "/forgot", "/reset", "/otp/**",
                                "/oauth2/**", "/login/oauth2/**", "/error", "/auth/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/", "/main-home", "/home", "/index", "/about", "/contact",
                                "/products/**", "/GuestProducts/**", "/fragments/**",
                                "/uploads/**", "/assets/**", "/ws/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/manager/**").hasAuthority("MANAGER")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER")
                        .requestMatchers("/chat/start", "/chat/customer/**")
                        .hasAnyAuthority("CUSTOMER","MANAGER","ADMIN")
                        .requestMatchers("/chat/inbox", "/chat/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                // Tắt form login & logout mặc định
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                // ✅ OAuth2 Login
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui.userService(oAuth2UserServiceImpl))
                        .successHandler(oAuth2SuccessHandler)
                );

        // ✅ Thêm JWT filter trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // ✅ Đăng ký Provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
