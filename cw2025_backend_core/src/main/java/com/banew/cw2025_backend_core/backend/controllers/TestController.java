package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.UserLoginForm;
import com.banew.cw2025_backend_common.dto.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class TestController {
    UserProfileService userProfileService;

    @PostMapping("/login")
    public UserTokenFormResult login(@Valid @RequestBody UserLoginForm form) {
        return userProfileService.login(form);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserTokenFormResult register(@Valid @RequestBody UserRegisterForm form) {
        return userProfileService.register(form);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/hello")
    public String check() {
        return "Привіт!";
    }
}
