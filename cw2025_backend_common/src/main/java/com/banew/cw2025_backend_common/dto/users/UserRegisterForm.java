package com.banew.cw2025_backend_common.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterForm (
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 5, max = 64)
        String username,
        String photoSrc,
        @NotBlank
        @Size(min = 8, max = 64)
        String password
) { }