package com.banew.cw2025_backend_core.backend.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class BasicConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(name = "admin.password")
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(
            @Value("${admin.username:sba-admin}") String username,
            @Value("${admin.password}") String password) {

        UserDetails admin = User.withUsername(username)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN_SERVICE")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Seezu API")
                                .version("v1")
                                .description("Backend API для мобільного застосунку Seezu")
                );
    }
}