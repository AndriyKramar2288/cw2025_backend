package com.banew.cw2025_backend_common.dto.courses;


import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Відображає CoursePlan в межах Course
 */
@Data
public class CoursePlanCourseDto {
    private Long id;
    @NotBlank
    private String name;
    private UserProfileBasicDto author;
    private String description;


}
