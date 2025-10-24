package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.UserLoginForm;
import com.banew.cw2025_backend_common.dto.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface UserProfileService {
    Converter<Jwt,? extends AbstractAuthenticationToken> myJwtAuthenticationConverter();
    Optional<UserProfile> getUserById(Long userId);
    UserTokenFormResult register(UserRegisterForm form);
    UserTokenFormResult login(UserLoginForm form);
}
