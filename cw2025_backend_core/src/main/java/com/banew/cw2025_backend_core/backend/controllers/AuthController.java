package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.users.UserLoginForm;
import com.banew.cw2025_backend_common.dto.users.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.users.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@AllArgsConstructor
@RestController
public class AuthController {
    private UserProfileService userProfileService;
    private BasicMapper basicMapper;

    @PostMapping("/login")
    public UserTokenFormResult login(@Valid @RequestBody UserLoginForm form) {
        return userProfileService.login(form);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserTokenFormResult register(@Valid @RequestBody UserRegisterForm form) {
        return userProfileService.register(form);
    }
}
