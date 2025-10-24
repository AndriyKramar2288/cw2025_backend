package com.banew.cw2025_backend_core.backend.services.implementations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${secret.decoder_key}")
    private String key;

    @Value("${token_lifetime}")
    private Long days_life_token;

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public String encodeJwt(UserProfile user) {
        Algorithm algorithm = Algorithm.HMAC256(key.getBytes());

        return JWT.create()
                .withSubject(Optional.ofNullable(user.getId()).orElseThrow(() -> new BadCredentialsException("Cannot generate JWT without user id!")).toString())
                .withIssuer(applicationName)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(LocalDateTime.now().plusDays(days_life_token).atZone(ZoneId.systemDefault()).toInstant()))
                .withArrayClaim("role", user.getRoles().toArray(new String[0]))
                .sign(algorithm);
    }
}
