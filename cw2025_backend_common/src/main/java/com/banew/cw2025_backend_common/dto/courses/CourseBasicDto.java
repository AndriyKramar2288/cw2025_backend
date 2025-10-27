package com.banew.cw2025_backend_common.dto.courses;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseBasicDto {
    private Instant startedAt;
    private CoursePlanCourseDto coursePlan;
    private List<TopicCompendiumDto> compendiums = new ArrayList<>();
}