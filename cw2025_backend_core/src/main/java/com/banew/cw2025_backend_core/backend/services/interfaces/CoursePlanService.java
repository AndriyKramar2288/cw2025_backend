package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;

public interface CoursePlanService {
    CoursePlanBasicDto createCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto);
    CoursePlanBasicDto updateCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto);
}
