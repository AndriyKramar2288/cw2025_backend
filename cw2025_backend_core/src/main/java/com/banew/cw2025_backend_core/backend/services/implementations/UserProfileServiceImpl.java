package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.users.*;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.JwtService;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private UserProfileRepository userProfileRepository;
    private PasswordEncoder passwordEncoder;
    private BasicMapper basicMapper;
    private JwtService jwtService;

    @Override
    public Converter<Jwt, ? extends AbstractAuthenticationToken> myJwtAuthenticationConverter() {
        return (getJwt) -> {
            Long userId = Long.parseLong(Optional.ofNullable(getJwt.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Getted jwt has no subject!")));

            UserProfile foundUser = getUserById(userId)
                    .orElseThrow(() -> new BadCredentialsException("User with id " + getJwt.getSubject() + " was not found!"));
            return new UsernamePasswordAuthenticationToken(foundUser, getJwt, foundUser.getAuthorities());
        };
    }

    @Override
    @Cacheable(value = "userProfileById", key = "#userId")
    public Optional<UserProfile> getUserById(Long userId) {
        return userProfileRepository.findById(userId);
    }

    @Override
    @Cacheable(value = "userProfileDetailedById", key = "#userId")
    public UserProfileDetailedDto getUserProfileDetailedById(Long userId) {
        var user = userProfileRepository.findByIdForDetailedDto(userId)
                .orElseThrow(() -> new MyBadRequestException(
                        "User with id " + userId + " was not found!"
                ));

        return basicMapper.userProfileToDetailedDto(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userProfileById", key = "#previousProfile.id"),
            @CacheEvict(value = "userProfileDetailedById", key = "#previousProfile.id")
    })
    public UserProfileBasicDto updateUser(UserProfileBasicDto dto, UserProfile previousProfile) {

        if (dto.email() != null) previousProfile.setEmail(dto.email());
        if (dto.photoSrc() != null) previousProfile.setPhotoSrc(dto.photoSrc());
        if (dto.username() != null) previousProfile.setUsername(dto.username());

        userProfileRepository.save(previousProfile);
        
        return basicMapper.userProfileToBasicDto(previousProfile);
    }

    @Override
    public UserTokenFormResult register(UserRegisterForm form) {
        if (userProfileRepository.findByEmail(form.email()).isPresent())
            throw new RuntimeException("User with email \"" + form.email() + "\" is already exist!");

        UserProfile user = basicMapper.registerFormToUserProfile(form);
        user.setRoles(List.of("USER"));
        user.setPassword(passwordEncoder.encode(form.password()));

        userProfileRepository.save(user);

        String token = jwtService.encodeJwt(user);

        return new UserTokenFormResult (
                token,
                "Successful registration!",
                basicMapper.userProfileToBasicDto(user),
                201
        );
    }

    @Override
    public UserTokenFormResult login(UserLoginForm form) {
        var ex = new MyBadRequestException(
                "Email or password is not correct!"
        );

        UserProfile user = userProfileRepository.findByEmail(form.email()).orElseThrow(() -> ex);
        if (!passwordEncoder.matches(form.password(), user.getPassword())) throw ex;

        String token = jwtService.encodeJwt(user);

        return new UserTokenFormResult (
                token,
                "Successful login!",
                basicMapper.userProfileToBasicDto(user),
                200
        );
    }
}
