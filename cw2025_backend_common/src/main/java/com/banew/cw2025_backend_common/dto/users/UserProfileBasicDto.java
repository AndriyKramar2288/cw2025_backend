package com.banew.cw2025_backend_common.dto.users;

public record UserProfileBasicDto (
        String username,
        String email,
        String photoSrc
) { }