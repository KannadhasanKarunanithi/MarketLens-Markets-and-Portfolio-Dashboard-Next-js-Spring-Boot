package com.marketlens.common.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.marketlens.common.error.UnauthorizedException;

public class CurrentUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String displayName;
    private final List<GrantedAuthority> authorities;

    public CurrentUser(UUID id, String username, String displayName, List<String> roles) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    public UUID id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static CurrentUser require() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CurrentUser user) {
            return user;
        }
        throw new UnauthorizedException("Authentication required");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
