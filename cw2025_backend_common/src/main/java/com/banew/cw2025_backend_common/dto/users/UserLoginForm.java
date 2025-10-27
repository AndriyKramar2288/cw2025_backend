package com.banew.cw2025_backend_common.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginForm {
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 8)
    private String password;
}
