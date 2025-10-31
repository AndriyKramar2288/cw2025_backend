package com.banew.cw2025_backend_common.dto.courses;

import java.time.Instant;
import java.util.List;

public record CourseBasicDto (
        Instant startedAt,
        CoursePlanCourseDto coursePlan,
        List<TopicCompendiumDto> compendiums
) {}