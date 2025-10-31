package com.banew.cw2025_backend_common.dto.coursePlans;

import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CoursePlanBasicDto (
        Long id,
        @NotBlank String name,
        UserProfileBasicDto author,
        String description,
        @Size(min = 1) @NotNull List<TopicBasicDto> topics
) {
    public record TopicBasicDto (
            Long id,
            @NotBlank
            String name,
            String description
    ) { }
}
