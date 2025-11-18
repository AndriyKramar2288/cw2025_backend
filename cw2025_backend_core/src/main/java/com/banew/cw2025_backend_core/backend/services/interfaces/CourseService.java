package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;

import java.util.List;

public interface CourseService {
    List<CourseBasicDto> getUserCourses(UserProfile currentUser);
    CourseDetailedDto getCourseById(UserProfile currentUser, Long courseId);
    CourseBasicDto beginCourse(Long courseId, UserProfile currentUser);
    CourseDetailedDto endCourse(Long courseId, UserProfile currentUser);
    TopicCompendiumDto beginTopic(Long topicId, UserProfile currentUser, Long courseId);
    TopicCompendiumDto updateCompendium(TopicCompendiumDto topicCompendiumDto, UserProfile currentUser, Long courseId);
}