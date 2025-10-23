package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;

import net.codejava.utea.auth.security.OAuth2LoginSuccessHandler;
import net.codejava.utea.auth.security.JwtAuthFilter;
import net.codejava.utea.auth.security.JwtAuthenticationEntryPoint;
import net.codejava.utea.auth.service.CustomOAuth2UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserServiceImpl;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // DEV
        // return new BCryptPasswordEncoder();    // PROD
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(
                  "/api/auth/**", "/login", "/register", "/forgot", "/reset", "/otp/**",
                  "/oauth2/**", "/login/oauth2/**", "/error", "/auth/**",
                  "/css/**","/js/**","/images/**","/webjars/**"
              ).permitAll()
              .requestMatchers("/", "/main-home", "/home", "/index", "/about", "/contact",
                  "/products/**", "/GuestProducts/**", "/fragments/**", "/css/**", "/js/**",
                  "/images/**", "/uploads/**", "/assets/**", "/ws/**").permitAll()
              .requestMatchers("/admin/**").hasAuthority("ADMIN")
              .requestMatchers("/manager/**").hasAuthority("MANAGER")
              .requestMatchers("/customer/**").hasAuthority("CUSTOMER") 
              .requestMatchers("/shipper/**").hasAuthority("SHIPPER")
              .requestMatchers("/chat/start", "/chat/customer/**").hasAnyAuthority("CUSTOMER","MANAGER","ADMIN")
              .requestMatchers("/chat/inbox", "/chat/admin/**").hasAuthority("ADMIN")
              .anyRequest().authenticated()
          )
          .formLogin(form -> form.disable())
          .logout(lg -> lg.disable())
          // ====== OAuth2 Login ======
          .oauth2Login(oauth -> oauth
        		    .loginPage("/login")
        		    .userInfoEndpoint(ui -> ui.userService(oAuth2UserServiceImpl)) // bean kiểu OAuth2UserService
        		    .successHandler(oAuth2SuccessHandler)
          );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
}
