package net.codejava.utea.auth.security;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth2 = super.loadUser(req);
        Map<String,Object> attrs = oauth2.getAttributes();

        String email = (String) attrs.get("email");
        String name  = (String) attrs.getOrDefault("name", email);

        // Tìm user theo email; nếu không có -> tạo mới + gán CUSTOMER
        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User u = User.builder()
                            .email(email)
                            .fullName(name)
                            .passwordHash(encoder.encode(UUID.randomUUID().toString())) // thoả NOT NULL
                            .status("ACTIVE")
                            .build();
                    u = userRepo.save(u);

                    Role customer = roleRepo.findByCode("CUSTOMER")
                            .orElseGet(() -> roleRepo.save(Role.builder().code("CUSTOMER").build()));
                    u.getRoles().add(customer);
                    return userRepo.save(u);
                });

        var authorities = user.getRoles().stream()
                .map(Role::getCode)
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // Key "email" là nameAttributeKey cho DefaultOAuth2User
        return new DefaultOAuth2User(authorities, attrs, "email");
    }
}
