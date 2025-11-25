package com.banew.cw2025_backend_common.dto.users;

import java.util.List;

public record UserProfileDetailedDto(
        Long id,
        String username,
        String email,
        String photoSrc,
        List<UserProfileCoursePlanDto> coursePlans
) {
}
