package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursePlanRepository extends ListCrudRepository<CoursePlan, Long> {
    @Query("select c from CoursePlan c left join c.courses crs group by c order by count(crs) desc")
    List<CoursePlan> findPopularCoursePlans(Pageable pageable);
    @Query("select c from CoursePlan c where lower(c.name) like lower(concat('%', :q, '%'))")
    List<CoursePlan> findByText(@Param("q") String text);
}