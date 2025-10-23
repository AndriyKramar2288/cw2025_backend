package com.banew.cw2025_backend.backend.services.interfaces;

import com.banew.cw2025_backend.backend.entities.UserProfile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface UserProfileService {
    Converter<Jwt,? extends AbstractAuthenticationToken> myJwtAuthenticationConverter();
    Optional<UserProfile> getUserById(Long userId);
}
