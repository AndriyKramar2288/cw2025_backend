package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileDetailedDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.UserProfileService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserProfileController {
    private UserProfileService userProfileService;

    @GetMapping("/")
    public UserProfileDetailedDto getSelfProfile(@AuthenticationPrincipal UserProfile userProfile) {
        return userProfileService.getUserProfileDetailedById(userProfile.getId());
    }

    @GetMapping("/{id}")
    public UserProfileDetailedDto getOtherProfileDetailedById(@PathVariable Long id) {
        return userProfileService.getUserProfileDetailedById(id);
    }

    @PatchMapping("/")
    public UserProfileBasicDto updateSelfProfile(
            @AuthenticationPrincipal UserProfile userProfile,
            @RequestBody @Valid UserProfileBasicDto newProfileDto
    ) {
        return userProfileService.updateUser(newProfileDto, userProfile);
    }
}