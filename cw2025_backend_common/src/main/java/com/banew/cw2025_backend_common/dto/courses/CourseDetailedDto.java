package com.banew.cw2025_backend_common.dto.courses;

import java.time.Instant;
import java.util.List;

public record CourseDetailedDto(
        Long id,
        Instant startedAt,
        CoursePlanCourseDto coursePlan,
        List<TopicCompendiumDto> compendiums,
        Long currentCompendiumId
) { }
