package net.codejava.utea.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
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

    /** loginId được set trong CustomUserDetailsService để phản ánh input đăng nhập */
    @Setter
    private String loginId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null) return java.util.List.of();

        // Chuẩn hoá IN HOA + trim để khớp mọi rule hasRole/hasAuthority
        return user.getRoles().stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()); // tránh trùng lặp
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // ưu tiên loginId (email/username được truyền khi login)
        if (loginId != null) return loginId;
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !"LOCKED".equalsIgnoreCase(user.getStatus()); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "ACTIVE".equalsIgnoreCase(user.getStatus()); }

    // ---- Helpers tiện dụng ----
    public Long getId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
    
    /** Lấy fullName từ User */
    public String getFullName() {
        String fn = user.getFullName();
        if (fn != null && !fn.isBlank()) return fn.trim();
        return null;
    }

    /** Hiển thị tên: ưu tiên fullName → username → local-part của email */
    public String getDisplayName() {
        // Ưu tiên fullName trước
        String fn = user.getFullName();
        if (fn != null && !fn.isBlank()) return fn.trim();
        
        String u = user.getUsername();
        if (u != null && !u.isBlank()) return u.trim();

        String em = user.getEmail();
        if (em != null) {
            int at = em.indexOf('@');
            return at > 0 ? em.substring(0, at) : em;
        }
        return "user";
    }

    /** Tóm tắt roles dạng "ADMIN,MANAGER" (IN HOA) */
    public String getRoleSummary() {
        if (user.getRoles() == null) return "";
        return user.getRoles().stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.joining(","));
    }
}
