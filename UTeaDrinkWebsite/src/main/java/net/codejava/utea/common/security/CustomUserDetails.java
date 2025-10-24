package net.codejava.utea.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;

    // ✅ thêm setter và field loginId
    @Setter
    private String loginId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null)
            return java.util.List.of();
        return user.getRoles().stream()
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // nếu đã có loginId (do CustomUserDetailsService set vào)
        if (loginId != null) return loginId;
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !"LOCKED".equalsIgnoreCase(user.getStatus()); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return "ACTIVE".equalsIgnoreCase(user.getStatus()); }

    // ---- Helper tiện dụng ----
    public Long getId() { return user.getId(); }

    public String getEmail() { return user.getEmail(); }

    public String getDisplayName() { return user.getFullName(); }

    public String getRoleSummary() {
        return user.getRoles() == null ? "" :
                user.getRoles().stream().map(Role::getCode).collect(Collectors.joining(","));
    }
}
