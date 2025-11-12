package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-plan")
@AllArgsConstructor
public class CoursePlanController {

    private CoursePlanService coursePlanService;

    @PostMapping("/")
    public CoursePlanBasicDto createCoursePlan(@AuthenticationPrincipal UserProfile currentUser,
                                               @RequestBody @Valid CoursePlanBasicDto dto) {
        return coursePlanService.createCoursePlan(currentUser, dto);
    }

    @PatchMapping("/")
    public CoursePlanBasicDto updateCoursePlan(@AuthenticationPrincipal UserProfile currentUser,
                                               @RequestBody @Valid CoursePlanBasicDto dto) {
        return coursePlanService.updateCoursePlan(currentUser, dto);
    }

    @GetMapping("/{courseId}")
    public CoursePlanBasicDto getCoursePlanById(@PathVariable Long courseId) {
        return coursePlanService.getCoursePlanById(courseId);
    }

    @GetMapping("/")
    public List<CoursePlanBasicDto> getAllExistingPlans() {
        return coursePlanService.getAllExistingPlans();
    }

    @GetMapping("/examples")
    public List<CoursePlanBasicDto> getExamples() {
        return exampleCoursePlans();
    }

    private static List<CoursePlanBasicDto> exampleCoursePlans() {
        var author = new UserProfileBasicDto(2L, "John", "johndoe@gmail.com", "John Doe");

        return List.of(
                new CoursePlanBasicDto(
                        1L,
                        "Java Basics",
                        author,
                        "Learn the basics of Java programming.",
                        List.of(
                                new CoursePlanBasicDto.TopicBasicDto(1L, "Syntax", "Introduction to Java syntax."),
                                new CoursePlanBasicDto.TopicBasicDto(2L, "OOP", "Understanding classes and objects.")
                        )
                ),
                new CoursePlanBasicDto(
                        2L,
                        "Spring Boot Fundamentals",
                        author,
                        "A beginner-friendly introduction to Spring Boot.",
                        List.of(
                                new CoursePlanBasicDto.TopicBasicDto(3L, "Controllers", "REST endpoints and request mapping."),
                                new CoursePlanBasicDto.TopicBasicDto(4L, "JPA", "Integrating databases with Spring Data.")
                        )
                ),
                new CoursePlanBasicDto(
                        3L,
                        "Android Development",
                        author,
                        "Create modern Android apps with Kotlin.",
                        List.of(
                                new CoursePlanBasicDto.TopicBasicDto(5L, "Layouts", "Views, ViewGroups and XML."),
                                new CoursePlanBasicDto.TopicBasicDto(6L, "Activities", "Lifecycle and navigation.")
                        )
                ),
                new CoursePlanBasicDto(
                        4L,
                        "Algorithms & Data Structures",
                        author,
                        "Core computer science fundamentals.",
                        List.of(
                                new CoursePlanBasicDto.TopicBasicDto(7L, "Sorting", "QuickSort, MergeSort, etc."),
                                new CoursePlanBasicDto.TopicBasicDto(8L, "Trees", "Binary trees and traversals.")
                        )
                ),
                new CoursePlanBasicDto(
                        5L,
                        "Database Design",
                        author,
                        "Learn to design normalized relational databases.",
                        List.of(
                                new CoursePlanBasicDto.TopicBasicDto(9L, "Normalization", "1NF, 2NF, 3NF explained."),
                                new CoursePlanBasicDto.TopicBasicDto(10L, "ER Modeling", "Entity-relationship diagrams.")
                        )
                )
        );
    }
}
