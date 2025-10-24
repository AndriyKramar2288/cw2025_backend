package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Compendium;
import org.springframework.data.repository.ListCrudRepository;

public interface CompendiumRepository extends ListCrudRepository<Compendium, Long> {
}