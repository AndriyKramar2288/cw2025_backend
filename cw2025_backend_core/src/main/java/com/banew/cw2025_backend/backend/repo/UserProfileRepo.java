package com.banew.cw2025_backend.backend.repo;

import com.banew.cw2025_backend.backend.entities.UserProfile;
import org.springframework.data.repository.CrudRepository;

public interface UserProfileRepo extends CrudRepository<UserProfile, Long> {
}
