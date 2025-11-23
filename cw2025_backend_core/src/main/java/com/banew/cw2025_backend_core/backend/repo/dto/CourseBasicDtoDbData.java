package com.banew.cw2025_backend_core.backend.repo.dto;

import com.banew.cw2025_backend_core.backend.entities.CoursePlan;

import java.time.Instant;

public record CourseBasicDtoDbData (
        Long id,
        Instant startedAt,
        CoursePlan coursePlan,
        String currentTopic,
        Long totalConcepts,
        Integer totalTopics
) {}
