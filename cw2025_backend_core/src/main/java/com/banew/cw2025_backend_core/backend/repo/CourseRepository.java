package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Course;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends ListCrudRepository<Course, Long> {
    Optional<Course> findByStudentAndCoursePlan(UserProfile student, CoursePlan coursePlan);
    List<Course> findByStudent(UserProfile student);
}