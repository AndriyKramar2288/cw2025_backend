package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.RecommendedConcept;
import org.springframework.data.repository.ListCrudRepository;

public interface RecommendedConceptRepository extends ListCrudRepository<RecommendedConcept, Long> {
}