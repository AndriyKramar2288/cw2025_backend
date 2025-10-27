package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Course;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CourseRepository extends ListCrudRepository<Course, Long> {
    List<Course> findByStudent(UserProfile student);
}