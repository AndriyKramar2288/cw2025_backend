package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
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

    @GetMapping("/search")
    public List<CoursePlanBasicDto> getPlansBySearchQuery(@RequestParam(value = "query", required = false)
                                                              String searchQuery) {
        return coursePlanService.getPlansBySearchQuery(searchQuery);
    }

    @GetMapping("/")
    public List<CoursePlanBasicDto> getAllExistingPlans() {
        return coursePlanService.getAllExistingPlans();
    }
}
