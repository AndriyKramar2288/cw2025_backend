package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.users.UserLoginForm;
import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.users.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.JwtService;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
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
                    .orElseThrow(() -> new BadCredentialsException("User with id " + getJwt.getSubject() + " is not found!"));
            return new UsernamePasswordAuthenticationToken(foundUser, getJwt, foundUser.getAuthorities());
        };
    }

    @Override
    public Optional<UserProfile> getUserById(Long userId) {
        return userProfileRepository.findById(userId);
    }

    @Override
    public UserProfileBasicDto updateUser(UserProfileBasicDto dto, UserProfile previousProfile) {

        if (dto.getEmail() != null) previousProfile.setEmail(dto.getEmail());
        if (dto.getPhotoSrc() != null) previousProfile.setPhotoSrc(dto.getPhotoSrc());
        if (dto.getUsername() != null) previousProfile.setUsername(dto.getUsername());

        userProfileRepository.save(previousProfile);
        
        return basicMapper.userProfileToBasicDto(previousProfile);
    }

    @Override
    public UserTokenFormResult register(UserRegisterForm form) {
        if (userProfileRepository.findByEmail(form.getEmail()).isPresent())
            throw new RuntimeException("User with email \"" + form.getEmail() + "\" is already exist!");

        UserProfile user = basicMapper.registerFormToUserProfile(form);
        user.setRoles(List.of("USER"));
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        userProfileRepository.save(user);

        String token = jwtService.encodeJwt(user);

        return UserTokenFormResult.builder()
                .token(token)
                .code(201)
                .message("Successful registration!")
                .userProfile(basicMapper.userProfileToBasicDto(user))
                .build();
    }

    @Override
    public UserTokenFormResult login(UserLoginForm form) {
        var ex = new MyBadRequestException(
                "Email or password is not correct!"
        );

        UserProfile user = userProfileRepository.findByEmail(form.getEmail()).orElseThrow(() -> ex);
        if (!passwordEncoder.matches(form.getPassword(), user.getPassword())) throw ex;

        String token = jwtService.encodeJwt(user);

        return UserTokenFormResult.builder()
                .token(token)
                .code(200)
                .message("Successful login!")
                .userProfile(basicMapper.userProfileToBasicDto(user))
                .build();
    }
}
