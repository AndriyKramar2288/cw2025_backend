package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Concept;
import com.banew.cw2025_backend_core.backend.entities.Course;
import org.springframework.data.repository.ListCrudRepository;

public interface ConceptRepository extends ListCrudRepository<Concept, Long> {
    long countByCompendium_Course(Course course);
}