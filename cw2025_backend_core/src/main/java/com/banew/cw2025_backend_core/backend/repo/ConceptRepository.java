package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Concept;
import com.banew.cw2025_backend_core.backend.entities.Course;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ConceptRepository extends ListCrudRepository<Concept, Long> {
    long countByCompendium_Course(Course course);

    @Modifying
    @Transactional
    @Query("""
            delete from Concept c
            where c.compendium.course.id = ?1
            """)
    void deleteByCourseId(Long id);
}