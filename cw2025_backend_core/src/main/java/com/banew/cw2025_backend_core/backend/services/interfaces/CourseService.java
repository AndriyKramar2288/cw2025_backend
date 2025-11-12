package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;

import java.util.List;

public interface CourseService {
    List<CourseBasicDto> getUserCourses(UserProfile currentUser);
    CourseBasicDto beginCourse(Long courseId, UserProfile currentUser);
    TopicCompendiumDto beginTopic(Long topicId, UserProfile currentUser);
    TopicCompendiumDto updateCompendium(TopicCompendiumDto topicCompendiumDto, UserProfile currentUser);
}