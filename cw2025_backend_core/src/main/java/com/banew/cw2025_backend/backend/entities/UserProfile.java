package com.banew.cw2025_backend.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
public class UserProfile {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String email;

    private String photoSrc;
    private List<String> roles;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map((e) -> new SimpleGrantedAuthority("ROLE_" + e)).toList();
    }
}
