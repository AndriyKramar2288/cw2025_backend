package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;

import java.util.List;

public interface CoursePlanService {
    CoursePlanBasicDto createCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto);
    CoursePlanBasicDto updateCoursePlan(UserProfile currentUser, Long id, CoursePlanBasicDto dto);
    List<CoursePlanBasicDto> getAllExistingPlans(UserProfile currentUser);
    List<CoursePlanBasicDto> getPlansBySearchQuery(UserProfile currentUser, String query);
    CoursePlanBasicDto getCoursePlanById(UserProfile currentUser, Long id);
    void evictByAuthorId(Long authorId);
}