package com.banew.cw2025_backend.backend.services.implementations;

import com.banew.cw2025_backend.backend.entities.UserProfile;
import com.banew.cw2025_backend.backend.repo.UserProfileRepo;
import com.banew.cw2025_backend.backend.services.interfaces.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private UserProfileRepo userProfileRepo;

    @Override
    public Converter<Jwt, ? extends AbstractAuthenticationToken> myJwtAuthenticationConverter() {
        return (getJwt) -> {
            Long userId = Long.parseLong(Optional.ofNullable(getJwt.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Getted jwt has no subject!")));

            UserProfile foundUser = getUserById(userId)
                    .orElseThrow(() -> new BadCredentialsException("User with id " + getJwt.getSubject() + " is not found!"));
            return new UsernamePasswordAuthenticationToken(foundUser, getJwt, foundUser.getAuthorities());
        };
    }

    @Override
    public Optional<UserProfile> getUserById(Long userId) {
        return userProfileRepo.findById(userId);
    }
}
