package com.banew.cw2025_backend_common.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginForm (
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 8)
        String password
) { }
