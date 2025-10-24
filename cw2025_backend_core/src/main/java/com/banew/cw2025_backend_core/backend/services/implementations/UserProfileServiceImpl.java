package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.UserLoginForm;
import com.banew.cw2025_backend_common.dto.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.JwtService;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
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
    public UserTokenFormResult register(UserRegisterForm form) {
        if (userProfileRepository.findByEmail(form.getEmail()).isPresent())
            throw new RuntimeException("User with email \"" + form.getEmail() + "\" is already exist!");

        UserProfile user = new UserProfile();
        user.setRoles(List.of("USER"));
        user.setEmail(form.getEmail());
        user.setUsername(form.getName());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userProfileRepository.save(user);

        String token = jwtService.encodeJwt(user);

        return UserTokenFormResult.builder()
                .token(token)
                .code(201)
                .message("Successful registration!")
                .build();
    }

    @Override
    public UserTokenFormResult login(UserLoginForm form) {
        RuntimeException ex = new RuntimeException(
                "Email or password is not correct!"
        );

        UserProfile user = userProfileRepository.findByEmail(form.getEmail()).orElseThrow(() -> ex);
        if (!passwordEncoder.matches(user.getPassword(), form.getPassword())) throw ex;

        String token = jwtService.encodeJwt(user);

        return UserTokenFormResult.builder()
                .token(token)
                .code(200)
                .message("Successful login!")
                .build();
    }
}
