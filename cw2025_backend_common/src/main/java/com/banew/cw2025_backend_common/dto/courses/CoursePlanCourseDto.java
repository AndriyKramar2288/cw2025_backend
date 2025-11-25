package com.banew.cw2025_backend_common.dto.courses;


import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import jakarta.validation.constraints.NotBlank;

/**
 * Відображає CoursePlan в межах Course
 */
public record CoursePlanCourseDto (
        Long id,
        @NotBlank
        String name,
        UserProfileBasicDto author,
        String description
) { }
