package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);

    @Query("""
            select u from UserProfile u
            left join fetch u.coursePlans
            where u.id = ?1
            """)
    Optional<UserProfile> findByIdForDetailedDto(Long id);

}
