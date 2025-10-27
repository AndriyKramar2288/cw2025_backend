package com.banew.cw2025_backend_common.dto.coursePlans;

import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CoursePlanBasicDto {
    private Long id;
    @NotBlank
    private String name;
    private UserProfileBasicDto author;
    private String description;
    @Size(min = 1)
    @NotNull
    private List<TopicBasicDto> topics;

    @Data
    public static class TopicBasicDto {
        private Long id;
        @NotBlank
        private String name;
        private String description;
    }
}
