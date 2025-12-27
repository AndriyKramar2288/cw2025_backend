package com.banew.cw2025_backend_core.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue
    @NotNull
    private Long id;
    @Column(length = 64)
    @NotBlank
    private String username;
    @Column(length = 255)
    @NotBlank
    private String password;
    @Column(length = 99, unique = true)
    @NotBlank
    private String email;
    @Column(length = 512)
    @Nullable
    private String photoSrc;
    @Column(length = 64)
    @NotNull
    private List<String> roles = List.of("USER");
    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @NotNull
    private Set<CoursePlan> coursePlans = new LinkedHashSet<>();

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map((e) -> new SimpleGrantedAuthority("ROLE_" + e)).toList();
    }
}
