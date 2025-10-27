package com.banew.cw2025_backend_common.dto.courses;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import lombok.Data;

import java.util.List;

@Data
public class TopicCompendiumDto {
    private long id;
    private String notes;
    private CoursePlanBasicDto.TopicBasicDto topic;
    private List<ConceptBasicDto> concepts;

    @Data
    public static class ConceptBasicDto {
        private Long id;
        private String name;
        private String description;
    }
}
