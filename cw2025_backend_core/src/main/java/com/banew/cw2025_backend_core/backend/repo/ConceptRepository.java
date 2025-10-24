package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Concept;
import org.springframework.data.repository.ListCrudRepository;

public interface ConceptRepository extends ListCrudRepository<Concept, Long> {
}