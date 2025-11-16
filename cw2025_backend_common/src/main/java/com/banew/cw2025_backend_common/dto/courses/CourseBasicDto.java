package com.banew.cw2025_backend_common.dto.courses;

import java.time.Instant;

public record CourseBasicDto (
        Long id,
        Instant startedAt,
        CoursePlanCourseDto coursePlan,
        String currentTopic,
        Long totalConcepts,
        Long totalTopics
) {}