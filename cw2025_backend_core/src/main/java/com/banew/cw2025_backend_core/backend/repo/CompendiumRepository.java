package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Compendium;
import com.banew.cw2025_backend_core.backend.entities.Course;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CompendiumRepository extends ListCrudRepository<Compendium, Long> {

    @Query("select c from Compendium c left join fetch c.concepts where c.id = ?1")
    Optional<Compendium> findByIdWithConcepts(long id);

    @Query("select c from Compendium c where c.topic.id = ?1 and c.course.student = ?2 and c.index = ?3")
    Optional<Compendium> findByTopicIdAndStudentAndIndex(long id, UserProfile student, int index);

    @Query("""
            select c from Compendium c
            join fetch c.course
            join fetch c.topic
            left join fetch c.course.currentCompendium cc
            left join fetch cc.concepts
            where c.topic.id = ?1 and c.course.student = ?2
            """)
    Optional<Compendium> findByTopicIdAndStudentWithCourse(long id, UserProfile student);

    long countByCourse(Course course);

    @Modifying
    @Transactional
    @Query("""
            delete from Compendium c
            where c.course.id = ?1
            """)
    void deleteByCourseId(Long id);
}