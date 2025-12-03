package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-plan")
@AllArgsConstructor
public class CoursePlanController {

    private CoursePlanService coursePlanService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public CoursePlanBasicDto createCoursePlan(@AuthenticationPrincipal UserProfile currentUser,
                                               @RequestBody @Valid CoursePlanBasicDto dto) {
        return coursePlanService.createCoursePlan(currentUser, dto);
    }

    @GetMapping("/{courseId}")
    public CoursePlanBasicDto getCoursePlanById(@AuthenticationPrincipal UserProfile currentUser,
                                                @PathVariable Long courseId) {
        return coursePlanService.getCoursePlanById(currentUser, courseId);
    }

    @PatchMapping("/{courseId}")
    public CoursePlanBasicDto updateCoursePlan(@AuthenticationPrincipal UserProfile currentUser,
                                                @PathVariable Long courseId,
                                               @RequestBody @Valid CoursePlanBasicDto dto) {
        return coursePlanService.updateCoursePlan(currentUser, courseId, dto);
    }

    @GetMapping("/search")
    public List<CoursePlanBasicDto> getPlansBySearchQuery(@AuthenticationPrincipal UserProfile currentUser,
                                                          @RequestParam(value = "query", required = false)
                                                          String searchQuery) {
        return coursePlanService.getPlansBySearchQuery(currentUser, searchQuery);
    }

    @GetMapping("/")
    public List<CoursePlanBasicDto> getAllExistingPlans(@AuthenticationPrincipal UserProfile currentUser) {
        return coursePlanService.getAllExistingPlans(currentUser);
    }
}
