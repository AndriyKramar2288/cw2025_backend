package com.banew.cw2025_backend_common.dto.courses;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;

import java.util.List;

public record TopicCompendiumDto (
        long id,
        String notes,
        CoursePlanBasicDto.TopicBasicDto topic,
        List<ConceptBasicDto> concepts
) {

    public record ConceptBasicDto (
        Long id,
        String name,
        String description
    ) { }
}
