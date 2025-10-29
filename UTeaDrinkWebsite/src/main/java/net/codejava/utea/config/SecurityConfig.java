package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.security.CustomAccessDeniedHandler;
import net.codejava.utea.auth.security.JwtAuthFilter;
import net.codejava.utea.auth.security.JwtAuthenticationEntryPoint;
import net.codejava.utea.auth.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // để @PreAuthorize hoạt động
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;

    // Bean OAuth2UserService duy nhất (Impl của bạn)
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;

    // PasswordEncoder được tiêm từ PasswordConfig (hoặc bean hiện có của bạn)
    private final PasswordEncoder passwordEncoder;

    /**
     * ✅ QUAN TRỌNG: bỏ prefix "ROLE_"
     * Khi có bean này, mọi hasRole('MANAGER') sẽ khớp authority "MANAGER"
     * (không cần đổi DB hay đổi CustomUserDetails).
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // empty prefix
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // OAuth2 Authorization Code cần session ngắn để giữ state / code-verifier
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .csrf(csrf -> csrf.disable())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(entryPoint)      // 401: Chưa đăng nhập
                        .accessDeniedHandler(accessDeniedHandler)  // 403: Không có quyền
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/main-home", "/error", "/error/**",
                                "/api/auth/**", "/login", "/register", "/forgot", "/reset", "/otp/**",
                                "/oauth2/**", "/login/oauth2/**", "/auth/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/index", "/about", "/contact",
                                "/products/**", "/GuestProducts/**", "/fragments/**",
                                "/uploads/**", "/assets/**", "/ws/**",
                                "/error/403"  // Cho phép truy cập trang 403
                        ).permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/customer/cart/api/**").hasAnyRole("CUSTOMER","ADMIN","MANAGER") // ✅
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/manager/**").hasAuthority("MANAGER")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER")
                        .requestMatchers("/chat/manager/**").hasAuthority("MANAGER")
                        .requestMatchers("/chat/customer").hasAnyAuthority("CUSTOMER","MANAGER")
                        .anyRequest().authenticated()
                )

                // Không dùng formLogin/logout mặc định (bạn đã có /login POST + JWT)
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // OAuth2 Login: dùng service Impl + success handler set JWT cookie + redirect
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                // JWT filter cho các request sau khi đã có cookie UTEA_TOKEN
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
