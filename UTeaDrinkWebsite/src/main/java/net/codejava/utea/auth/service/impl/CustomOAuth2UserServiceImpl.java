package net.codejava.utea.auth.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("customOAuth2UserServiceImpl")
@Primary
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl
        extends DefaultOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegate = super.loadUser(userRequest);
        Map<String, Object> attrs = delegate.getAttributes();

        final String registrationId = Optional.ofNullable(userRequest.getClientRegistration().getRegistrationId())
                .map(String::toLowerCase).orElse("unknown");

        final String email = extractEmail(registrationId, attrs);
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email không tồn tại hoặc không được cấp từ provider: " + registrationId);
        }

        final String name  = extractName(registrationId, attrs);

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .fullName(name != null ? name : email)
                    .status("ACTIVE")
                    .passwordHash(passwordEncoder.encode("OAUTH2@" + UUID.randomUUID()))
                    .build();
            u = userRepository.save(u);

            Role roleCustomer = roleRepository.findByCode("CUSTOMER")
                    .orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").build()));
            u.getRoles().add(roleCustomer);
            return userRepository.save(u);
        });

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        String nameKey = resolveNameAttributeKey(userRequest, attrs);
        return new DefaultOAuth2User((Collection<? extends GrantedAuthority>) authorities, attrs, nameKey);
    }

    private String extractEmail(String registrationId, Map<String, Object> attrs) {
        Object v = attrs.get("email");
        if (v instanceof String s && !s.isBlank()) return s;
        return null;
    }

    private String extractName(String registrationId, Map<String, Object> attrs) {
        Object v = attrs.get("name");
        if (v instanceof String s && !s.isBlank()) return s;
        String given = (String) attrs.getOrDefault("given_name", "");
        String family = (String) attrs.getOrDefault("family_name", "");
        String combined = (given + " " + family).trim();
        return combined.isEmpty() ? null : combined;
    }

    private String resolveNameAttributeKey(OAuth2UserRequest req, Map<String, Object> attrs) {
        String configured = req.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName(); // Google thường là "sub"
        if (configured != null && !configured.isBlank()) return configured;
        if (attrs.containsKey("email")) return "email";
        return "sub";
    }
}
