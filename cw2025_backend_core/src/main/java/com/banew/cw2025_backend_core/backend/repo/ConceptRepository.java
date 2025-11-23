package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Concept;
import com.banew.cw2025_backend_core.backend.entities.Course;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ConceptRepository extends ListCrudRepository<Concept, Long> {
    long countByCompendium_Course(Course course);

    @Query("select count(c) from Concept c where c.compendium.course = ?1")
    long qwewqweqweqw(Course course);
}