package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoursePlanRepository extends ListCrudRepository<CoursePlan, Long> {

    @Query("""
            select c.id from CoursePlan c
            left join c.courses crs
            group by c order by count(crs) desc
            """)
    List<Long> findPopularCoursePlanIds(Pageable pageable);

    @Query("""
       select distinct c from CoursePlan c
       left join fetch c.topics
       join fetch c.author
       where c.id in :ids
       """)
    List<CoursePlan> findCoursesByIdForBasicDto(@Param("ids") List<Long> ids);

    @Query("""
            select c from CoursePlan c
            left join fetch c.topics
            join fetch c.author
            where lower(c.name) like lower(concat('%', :q, '%'))
            """)
    List<CoursePlan> findByText(@Param("q") String text);

    @Query("select c from CoursePlan c left join fetch c.topics join fetch c.author")
    List<CoursePlan> findCoursesForBasicDto();

    @Query("select cp from CoursePlan cp left join fetch cp.topics where cp.id = ?1")
    Optional<CoursePlan> findByIdWithTopics(Long courseId);
}