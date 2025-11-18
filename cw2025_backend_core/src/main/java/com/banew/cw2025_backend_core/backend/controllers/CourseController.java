package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/course")
public class CourseController {

    private CourseService courseService;

    @GetMapping("/")
    public List<CourseBasicDto> getUserCourses(@AuthenticationPrincipal UserProfile currentUser) {
        return courseService.getUserCourses(currentUser);
    }

    @GetMapping("/by-plan/{courseId}")
    public CourseDetailedDto getUserCourseById(@PathVariable(name = "courseId") Long courseId,
                                               @AuthenticationPrincipal UserProfile currentUser) {
        return courseService.getCourseById(currentUser, courseId);
    }

    @PostMapping("/by-plan/{courseId}/start")
    public CourseBasicDto beginCourse(@PathVariable(name = "courseId") Long courseId,
                                      @AuthenticationPrincipal UserProfile currentUser) {
        return courseService.beginCourse(courseId, currentUser);
    }

    @PostMapping("/by-plan/{courseId}/end")
    public CourseDetailedDto endCourse(@PathVariable(name = "courseId") Long courseId,
                                      @AuthenticationPrincipal UserProfile currentUser) {
        return courseService.endCourse(courseId, currentUser);
    }

    @PostMapping("/by-plan/{courseId}/topic/{topicId}/start")
    public TopicCompendiumDto beginTopic(@PathVariable(name = "courseId") Long courseId,
                                         @PathVariable(name = "topicId") Long topicId,
                                         @AuthenticationPrincipal UserProfile currentUser) {
        return courseService.beginTopic(topicId, currentUser, courseId);
    }

    @PutMapping("/by-plan/{courseId}/topic")
    public TopicCompendiumDto updateCompendium(@RequestBody TopicCompendiumDto topicCompendiumDto,
                                               @PathVariable(name = "courseId") Long courseId,
                                               @AuthenticationPrincipal UserProfile currentUser) {
        return courseService.updateCompendium(topicCompendiumDto, currentUser, courseId);
    }
}
