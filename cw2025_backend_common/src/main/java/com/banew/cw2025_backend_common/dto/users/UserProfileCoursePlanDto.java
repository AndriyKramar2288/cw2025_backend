package com.banew.cw2025_backend_common.dto.users;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;

import java.util.List;

/**
 * Відображає CoursePlan в межах UserProfileDetailedDto
 */
public record UserProfileCoursePlanDto(
        Long id,
        String name, String description,
        List<CoursePlanBasicDto.TopicBasicDto> topics,
        String backgroundSrc,
        Boolean isPublic
) {
}
