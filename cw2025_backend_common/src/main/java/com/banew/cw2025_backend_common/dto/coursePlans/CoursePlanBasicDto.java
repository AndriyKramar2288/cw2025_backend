package com.banew.cw2025_backend_common.dto.coursePlans;

import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CoursePlanBasicDto (
        Long id,
        @NotBlank @Size(min = 5, max = 255) String name,
        UserProfileBasicDto author,
        @Size(max = 2048) String description,
        @Size(min = 1, max = 100) @NotNull List<TopicBasicDto> topics,
        long studentCount,
        String backgroundSrc
) {
    public record TopicBasicDto (
            Long id,
            @NotBlank
            @Size(min = 5, max = 255)
            String name,
            @Size(max = 2048)
            String description
    ) { }
}
