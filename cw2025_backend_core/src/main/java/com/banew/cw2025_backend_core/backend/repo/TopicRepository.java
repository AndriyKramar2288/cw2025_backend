package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.Topic;
import org.springframework.data.repository.ListCrudRepository;

public interface TopicRepository extends ListCrudRepository<Topic, Long> {
}