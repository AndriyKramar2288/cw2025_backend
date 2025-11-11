package com.banew.cw2025_backend_common.dto.courses;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TopicCompendiumDto (
        long id,
        String notes,
        CoursePlanBasicDto.TopicBasicDto topic,
        List<ConceptBasicDto> concepts,
        CompendiumStatus status
) {

    public record ConceptBasicDto (
        Long id,
        @NotBlank
        @Size(min = 5, max = 255)
        String name,
        @NotBlank
        @Size(min = 5, max = 4096)
        String description
    ) { }
}
