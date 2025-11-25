package com.banew.cw2025_backend_core.backend.repo;

import com.banew.cw2025_backend_core.backend.entities.FlashCard;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FlashCardRepository extends ListCrudRepository<FlashCard, Long> {

    List<FlashCard> findByConcept_Compendium_Course_StudentAndLastReviewAfter(UserProfile student, Instant lastReview);

    @Query("""
            select f from FlashCard f
            join fetch f.concept
            where f.id = ?1 and f.concept.compendium.course.student = ?2
            """)
    Optional<FlashCard> findByIdAndStudent(long id, UserProfile student);

    @Query("""
            select f from FlashCard f
            join fetch f.concept
            where f.concept.compendium.course.student = ?1 and (f.dueReview is null or f.dueReview < ?2)""")
    List<FlashCard> availableCards(UserProfile student, Instant now);

    @Query("select count(f) from FlashCard f where f.concept.compendium.course.student = ?1 and f.lastReview is null")
    long countNewCards(UserProfile student);

    @Query("""
            select count(f) from FlashCard f
            where f.concept.compendium.course.student = ?1 and f.dueReview < ?2 and f.repetition <> 0""")
    long countReviewCards(UserProfile student, Instant now);

    @Query("""
            select count(f) from FlashCard f
            where f.concept.compendium.course.student = ?1 and f.dueReview < ?2 and f.repetition = 0""")
    long countStudyCards(UserProfile student, Instant now);
}