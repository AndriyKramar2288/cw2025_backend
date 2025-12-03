package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.users.*;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface UserProfileService {
    Converter<Jwt,? extends AbstractAuthenticationToken> myJwtAuthenticationConverter();
    Optional<UserProfile> getUserById(Long userId);
    UserProfileDetailedDto getUserProfileDetailedById(Long userId, UserProfile currentUser);
    UserProfileDetailedDto getUserProfileDetailed(UserProfile currentUser);
    UserProfileBasicDto updateUser(UserProfileBasicDto dto, UserProfile previousProfile);
    UserTokenFormResult register(UserRegisterForm form);
    UserTokenFormResult login(UserLoginForm form);
}