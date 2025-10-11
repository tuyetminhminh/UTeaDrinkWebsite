package net.codejava.utea.config;

import net.codejava.utea.security.JwtAuthFilter;
import net.codejava.utea.security.JwtAuthenticationEntryPoint;
import net.codejava.utea.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Dev only
        // return new BCryptPasswordEncoder();     // Prod
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép API auth truy cập mà không cần JWT
                        .requestMatchers("/api/auth/**", "/login", "/register", "/forgot", "/reset", "/otp/**").permitAll()
                        // Public (giữ như bạn đã cấu hình)
                        .requestMatchers("/", "/main-home", "/home", "/index",
                                "/about", "/contact",
                                "/products", "/products/**",
                                "/fragments/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/assets/**",
                                "/login", "/register", "/forgot", "/reset", "/otp/**",
                                "/ws/**" // WebSocket handshake
                        ).permitAll()


                        // Role-based (giữ y nguyên)
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/seller/**").hasAuthority("SELLER")
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER")
                        .requestMatchers("/chat/**").hasAnyAuthority("ADMIN", "CUSTOMER")

                        .anyRequest().authenticated()
                )
                ;

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // ✅ Gắn provider vào SecurityContext
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
}
