package net.codejava.utea.service.impl;

import net.codejava.utea.entity.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails, Principal {

    private final Account account;

    public CustomUserDetails(Account account) {
        this.account = account;
    }

    public Long getId() {
        return account.getId();
    }

    public String getDisplayName() {
        return account.getDisplayName();
    }

    public String getRole() {
        return account.getRole();
    }

    // Phương thức mới để lấy thông tin Account
    public Account getAccount() {
        return this.account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(account.getRole()));
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return account.isEnabled(); }

    @Override
    public String getName() {
        return getUsername();
    }
}
