package com.banew.cw2025_backend_common.dto.users;

public record UserTokenFormResult (
        String token,
        String message,
        UserProfileBasicDto userProfile,
        int code
) { }