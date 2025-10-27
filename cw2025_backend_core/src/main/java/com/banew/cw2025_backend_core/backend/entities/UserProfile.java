package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 64)
    private String username;
    @Column(length = 64)
    private String password;
    @Column(length = 99, unique = true)
    private String email;
    @Column(length = 255)
    private String photoSrc;
    @Column(length = 64)
    private List<String> roles;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map((e) -> new SimpleGrantedAuthority("ROLE_" + e)).toList();
    }
}
