package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Compendium;
import com.banew.cw2025_backend_core.backend.entities.Course;
import com.banew.cw2025_backend_core.backend.entities.Topic;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface CompendiumRepository extends ListCrudRepository<Compendium, Long> {
    Optional<Compendium> findByTopicAndCourse_Student(Topic topic, UserProfile student);
    Optional<Compendium> findByIndexAndTopic(int index, Topic topic);

    long countByCourse(Course course);
}