package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Compendium;
import com.banew.cw2025_backend_core.backend.entities.Course;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.repo.dto.CourseBasicDtoDbData;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends ListCrudRepository<Course, Long> {

    List<Course> findByStudent(UserProfile student);

    @Query("select (count(c) > 0) from Course c where c.student = ?1 and c.coursePlan.id = ?2")
    boolean existsByStudentAndCoursePlanId(UserProfile student, long id);

    @Query("""
            select c from Course c
            join fetch c.coursePlan cp
            join fetch cp.author
            left join fetch c.compendiums cmp
            left join fetch cmp.topic
            left join fetch cmp.concepts
            left join fetch c.currentCompendium
            where c.student.id = ?1 and c.coursePlan.id = ?2
            """)
    Optional<Course> findByStudentAndCoursePlanIdWithFetch(long userId, long coursePlanId);

    @Query("""
            select c from Course c
            left join fetch c.currentCompendium cmp
            left join fetch cmp.topic
            left join fetch cmp.concepts
            where c.student = ?1 and c.coursePlan.id = ?2
            """)
    Optional<Course> findByStudentAndCoursePlanId(UserProfile student, long id);

    @Query("""
            select c.id from Course c
            where c.student = ?1 and c.coursePlan.id = ?2
            """)
    Optional<Long> findIdByStudentAndCoursePlanId(UserProfile student, long id);

    @Query("""
    select new com.banew.cw2025_backend_core.backend.repo.dto.CourseBasicDtoDbData(
        crs.id,
        crs.startedAt,
        cp,
        t.name,
        (select count(c) from Concept c where c.compendium.course = crs),
        size(crs.compendiums)
    )
    from Course crs
    join crs.coursePlan cp
    join fetch cp.author au
    join crs.compendiums
    left join crs.currentCompendium cc
    left join cc.topic t
    where crs.student = ?1
    group by crs.id, crs.startedAt, cp, au, t.name
""")
    List<CourseBasicDtoDbData> findUserCourses(UserProfile student);

    @Query("select c from Course c join fetch c.student where c.coursePlan.id = ?1")
    List<Course> findByCoursePlanIdWithStudent(long id);

    @Transactional
    @Modifying
    @Query("update Course c set c.currentCompendium = ?1 where c.id = ?2")
    void updateCurrentCompendiumById(Compendium currentCompendium, long id);
}