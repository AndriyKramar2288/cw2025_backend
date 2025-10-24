package com.banew.cw2025_backend_common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginForm {
    @NotBlank
    private String email;
    @NotBlank
    @Min(8)
    private String password;
}
