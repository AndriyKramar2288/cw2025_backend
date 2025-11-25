package com.banew.cw2025_backend_common.dto.users;

public record UserProfileBasicDto (
        Long id,
        String username,
        String email,
        String photoSrc
) { }