package com.banew.cw2025_backend_common.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterForm {
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 5)
    private String username;
    private String photoSrc;
    @NotBlank
    @Size(min = 8)
    private String password;
}