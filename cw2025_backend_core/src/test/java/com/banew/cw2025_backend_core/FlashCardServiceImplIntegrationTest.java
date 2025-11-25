package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.cards.FlashCardAnswer;
import com.banew.cw2025_backend_common.dto.cards.FlashCardBasicDto;
import com.banew.cw2025_backend_common.dto.cards.FlashCardDayStats;
import com.banew.cw2025_backend_common.dto.cards.FlashCardType;
import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.ConceptRepository;
import com.banew.cw2025_backend_core.backend.repo.FlashCardRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.FlashCardService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlashCardServiceImplIntegrationTest {

    @Autowired
    private FlashCardService flashCardService;

    @Autowired
    private FlashCardRepository flashCardRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private EntityManager entityManager;

    private UserProfile testUser;
    private Course testCourse;
    private Compendium testCompendium;
    private Topic testTopic;
    private CoursePlan testCoursePlan;

    @BeforeEach
    void setUp() {
        // Створення тестового користувача
        testUser = new UserProfile();
        testUser.setEmail("test@example.com");
        testUser.setUsername("Test User");
        entityManager.persist(testUser);

        // Створення тестового курсу
        testCoursePlan = new CoursePlan();
        testCoursePlan.setName("Test Course Plan");
        entityManager.persist(testCoursePlan);

        testTopic = new Topic();
        testTopic.setName("Test Topic");
        testTopic.setCoursePlan(testCoursePlan);
        entityManager.persist(testTopic);

        testCourse = new Course();
        testCourse.setStudent(testUser);
        testCourse.setCoursePlan(testCoursePlan);
        entityManager.persist(testCourse);

        testCompendium = new Compendium();
        testCompendium.setCourse(testCourse);
        testCompendium.setTopic(testTopic);
        testCompendium.setStatus(CompendiumStatus.COMPLETED);
        entityManager.persist(testCompendium);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Отримання доступних карток для користувача")
    void testGetCards_ReturnsAvailableCards() {
        // Arrange
        FlashCard card1 = createFlashCard("Concept 1", "Description 1", null);
        FlashCard card2 = createFlashCard("Concept 2", "Description 2", Instant.now().minus(1, ChronoUnit.DAYS));
        FlashCard card3 = createFlashCard("Concept 3", "Description 3", Instant.now().plus(2, ChronoUnit.DAYS));

        // Act
        List<FlashCardBasicDto> result = flashCardService.getCards(testUser);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlashCardBasicDto::concept)
                .extracting(TopicCompendiumDto.ConceptBasicDto::name)
                .containsExactlyInAnyOrder("Concept 1", "Concept 2");
    }

    @Test
    @DisplayName("Отримання порожнього списку, коли немає доступних карток")
    void testGetCards_WhenNoAvailableCards_ReturnsEmptyList() {
        // Arrange
        createFlashCard("Future Card", "Description", Instant.now().plus(5, ChronoUnit.DAYS));

        // Act
        List<FlashCardBasicDto> result = flashCardService.getCards(testUser);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Оновлення концепту флешкартки")
    void testUpdateConcept_UpdatesConceptSuccessfully() {
        // Arrange
        FlashCard flashCard = createFlashCard("Old Name", "Old Description", null);
        TopicCompendiumDto.ConceptBasicDto newConcept = new TopicCompendiumDto.ConceptBasicDto(
                null, "New Name", "New Description", true
        );

        // Act
        FlashCardBasicDto result = flashCardService.updateConcept(testUser, flashCard.getId(), newConcept);

        // Assert
        assertThat(result.concept().name()).isEqualTo("New Name");
        assertThat(result.concept().description()).isEqualTo("New Description");

        Concept updatedConcept = conceptRepository.findById(flashCard.getConcept().getId()).orElseThrow();
        assertThat(updatedConcept.getName()).isEqualTo("New Name");
        assertThat(updatedConcept.getDescription()).isEqualTo("New Description");
    }

    @Test
    @DisplayName("Оновлення концепту - часткове оновлення")
    void testUpdateConcept_PartialUpdate() {
        // Arrange
        FlashCard flashCard = createFlashCard("Original Name", "Original Description", null);
        TopicCompendiumDto.ConceptBasicDto partialUpdate = new TopicCompendiumDto.ConceptBasicDto(
                null, "Updated Name", null, true
        );

        // Act
        FlashCardBasicDto result = flashCardService.updateConcept(testUser, flashCard.getId(), partialUpdate);

        // Assert
        assertThat(result.concept().name()).isEqualTo("Updated Name");
        assertThat(result.concept().description()).isEqualTo("Original Description");
    }

    @Test
    @DisplayName("Оновлення концепту - картка не знайдена")
    void testUpdateConcept_ThrowsException_WhenCardNotFound() {
        // Arrange
        TopicCompendiumDto.ConceptBasicDto newConcept = new TopicCompendiumDto.ConceptBasicDto(
                null, "New Name", "New Description", true
        );

        // Act & Assert
        assertThatThrownBy(() -> flashCardService.updateConcept(testUser, 999L, newConcept))
                .isInstanceOf(MyBadRequestException.class)
                .hasMessageContaining("Card with id '999' is no exists!");
    }

    @Test
    @DisplayName("Відповідь на картку - FAIL")
    void testAnswer_WithFailAnswer() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", null);
        flashCard.setRepetition(3);
        flashCard.setInterval(10);
        flashCard.setEasiness(2.5);
        flashCardRepository.save(flashCard);

        // Act
        FlashCardBasicDto result = flashCardService.answer(FlashCardAnswer.FAIL, testUser, flashCard.getId());

        // Assert
        FlashCard updated = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        assertThat(updated.getRepetition()).isZero();
        assertThat(updated.getInterval()).isZero();
        assertThat(updated.getLastReview()).isNotNull();
    }

    @Test
    @DisplayName("Відповідь на картку - GOOD (перше повторення)")
    void testAnswer_WithGoodAnswer_FirstRepetition() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", null);

        // Act
        FlashCardBasicDto result = flashCardService.answer(FlashCardAnswer.GOOD, testUser, flashCard.getId());

        // Assert
        FlashCard updated = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        assertThat(updated.getRepetition()).isEqualTo(1);
        assertThat(updated.getInterval()).isEqualTo(1.0);
        assertThat(updated.getLastReview()).isNotNull();
        assertThat(updated.getDueReview()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Відповідь на картку - GOOD (друге повторення)")
    void testAnswer_WithGoodAnswer_SecondRepetition() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", null);
        flashCard.setRepetition(1);
        flashCard.setInterval(1);
        flashCard.setEasiness(2.5);
        flashCard.setLastReview(Instant.now().minus(2, ChronoUnit.DAYS));
        flashCard.setDueReview(Instant.now().minus(1, ChronoUnit.DAYS));
        flashCardRepository.save(flashCard);

        // Act
        FlashCardBasicDto result = flashCardService.answer(FlashCardAnswer.GOOD, testUser, flashCard.getId());

        // Assert
        FlashCard updated = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        assertThat(updated.getRepetition()).isEqualTo(2);
        assertThat(updated.getInterval()).isEqualTo(6.0);
    }

    @Test
    @DisplayName("Відповідь на картку - EASY")
    void testAnswer_WithEasyAnswer() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", null);
        flashCard.setRepetition(2);
        flashCard.setInterval(6);
        flashCard.setEasiness(2.5);
        flashCard.setLastReview(Instant.now().minus(7, ChronoUnit.DAYS));
        flashCard.setDueReview(Instant.now().minus(1, ChronoUnit.DAYS));
        flashCardRepository.save(flashCard);

        // Act
        FlashCardBasicDto result = flashCardService.answer(FlashCardAnswer.EASY, testUser, flashCard.getId());

        // Assert
        FlashCard updated = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        assertThat(updated.getRepetition()).isEqualTo(3);
        assertThat(updated.getInterval()).isEqualTo(15.0); // 6 * 2.5
        assertThat(updated.getEasiness()).isGreaterThan(2.5); // Easiness збільшується для EASY
    }

    @Test
    @DisplayName("Відповідь на картку - помилка, коли час ще не прийшов")
    void testAnswer_ThrowsException_WhenNotTimeToReview() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", Instant.now().plus(2, ChronoUnit.DAYS));
        flashCard.setLastReview(Instant.now());
        flashCardRepository.save(flashCard);

        // Act & Assert
        assertThatThrownBy(() -> flashCardService.answer(FlashCardAnswer.GOOD, testUser, flashCard.getId()))
                .isInstanceOf(MyBadRequestException.class)
                .hasMessageContaining("You cannot review this card! The time has not yet come!");
    }

    @Test
    @DisplayName("Відповідь на картку - картка не знайдена")
    void testAnswer_ThrowsException_WhenCardNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> flashCardService.answer(FlashCardAnswer.GOOD, testUser, 999L))
                .isInstanceOf(MyBadRequestException.class)
                .hasMessageContaining("Card with id '999' is no exists!");
    }

    @Test
    @DisplayName("Перевірка доступних інтервалів у відповіді")
    void testAnswer_ReturnsAvailableIntervals() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test Concept", "Description", null);

        // Act
        FlashCardBasicDto result = flashCardService.answer(FlashCardAnswer.GOOD, testUser, flashCard.getId());

        // Assert
        assertThat(result.availableIntervals()).containsKeys(
                FlashCardAnswer.FAIL,
                FlashCardAnswer.BAD,
                FlashCardAnswer.GOOD,
                FlashCardAnswer.EASY
        );
    }

    @Test
    @DisplayName("Отримання статистики за день - без карток")
    void testGetDayStats_WhenNoCards() {
        // Act
        FlashCardDayStats stats = flashCardService.getDayStats(testUser);

        // Assert
        assertThat(stats.reviewNumber()).isZero();
        assertThat(stats.reviewDuration()).isEqualTo(Duration.ZERO);
        assertThat(stats.cardLefts()).containsEntry(FlashCardType.NEW, 0);
        assertThat(stats.cardLefts()).containsEntry(FlashCardType.REPEAT, 0);
        assertThat(stats.cardLefts()).containsEntry(FlashCardType.STUDY, 0);
    }

    @Test
    @DisplayName("Отримання статистики за день - з картками")
    void testGetDayStats_WithCards() {
        // Arrange
        // Нова картка (без lastReview)
        createFlashCard("New Card", "Description", null);

        // Картка для повторення (repetition != 0)
        FlashCard reviewCard = createFlashCard("Review Card", "Description", Instant.now().minus(1, ChronoUnit.HOURS));
        reviewCard.setRepetition(1);
        reviewCard.setLastReview(Instant.now().minus(2, ChronoUnit.HOURS));
        flashCardRepository.save(reviewCard);

        // Картка для вивчення (repetition == 0)
        FlashCard studyCard = createFlashCard("Study Card", "Description", Instant.now().minus(30, ChronoUnit.MINUTES));
        studyCard.setRepetition(0);
        studyCard.setLastReview(Instant.now().minus(1, ChronoUnit.HOURS));
        flashCardRepository.save(studyCard);

        // Картки, переглянуті сьогодні для статистики
        FlashCard todayCard1 = createFlashCard("Today 1", "Desc", null);
        todayCard1.setLastReview(Instant.now().minus(2, ChronoUnit.HOURS));
        flashCardRepository.save(todayCard1);

        FlashCard todayCard2 = createFlashCard("Today 2", "Desc", null);
        todayCard2.setLastReview(Instant.now().minus(1, ChronoUnit.HOURS));
        flashCardRepository.save(todayCard2);

        // Act
        FlashCardDayStats stats = flashCardService.getDayStats(testUser);

        // Assert
        assertThat(stats.cardLefts().get(FlashCardType.NEW)).isEqualTo(1);
        assertThat(stats.cardLefts().get(FlashCardType.REPEAT)).isEqualTo(1);
        assertThat(stats.cardLefts().get(FlashCardType.STUDY)).isEqualTo(1);
        assertThat(stats.reviewNumber()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Розрахунок тривалості перегляду")
    void testGetDayStats_CalculatesReviewDuration() {
        // Arrange
        Instant baseTime = Instant.now().minus(3, ChronoUnit.HOURS);

        FlashCard card1 = createFlashCard("Card 1", "Desc", null);
        card1.setLastReview(baseTime);
        flashCardRepository.save(card1);

        FlashCard card2 = createFlashCard("Card 2", "Desc", null);
        card2.setLastReview(baseTime.plus(5, ChronoUnit.SECONDS));
        flashCardRepository.save(card2);

        FlashCard card3 = createFlashCard("Card 3", "Desc", null);
        card3.setLastReview(baseTime.plus(10, ChronoUnit.SECONDS));
        flashCardRepository.save(card3);

        // Act
        FlashCardDayStats stats = flashCardService.getDayStats(testUser);

        // Assert
        assertThat(stats.reviewDuration()).isNotEqualTo(Duration.ZERO);
        assertThat(stats.reviewDuration().toSeconds()).isLessThan(15);
    }

    @Test
    @DisplayName("Ігнорування занадто довгих інтервалів між переглядами")
    void testGetDayStats_IgnoresLongIntervals() {
        // Arrange
        Instant baseTime = Instant.now().minus(2, ChronoUnit.HOURS);

        FlashCard card1 = createFlashCard("Card 1", "Desc", null);
        card1.setLastReview(baseTime);
        flashCardRepository.save(card1);

        // Інтервал більше 15 секунд - має бути проігнорований
        FlashCard card2 = createFlashCard("Card 2", "Desc", null);
        card2.setLastReview(baseTime.plus(20, ChronoUnit.SECONDS));
        flashCardRepository.save(card2);

        // Act
        FlashCardDayStats stats = flashCardService.getDayStats(testUser);

        // Assert
        // Тривалість має бути нульовою, оскільки єдиний інтервал > 15 секунд
        assertThat(stats.reviewDuration()).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Перевірка формули легкості (easiness)")
    void testEasinessCalculation() {
        // Arrange
        FlashCard flashCard = createFlashCard("Test", "Desc", null);
        double initialEasiness = 2.5;
        flashCard.setEasiness(initialEasiness);
        flashCardRepository.save(flashCard);

        // Act - відповідь EASY має збільшити easiness
        flashCardService.answer(FlashCardAnswer.EASY, testUser, flashCard.getId());

        // Assert
        FlashCard updated = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        assertThat(updated.getEasiness()).isGreaterThan(initialEasiness);
        double updatedEasiness = updated.getEasiness();

        // Act - відповідь FAIL має зменшити easiness
        flashCard.setDueReview(Instant.now().minus(1, ChronoUnit.DAYS));
        flashCardRepository.save(flashCard);
        flashCardService.answer(FlashCardAnswer.FAIL, testUser, flashCard.getId());

        FlashCard updatedAgain = flashCardRepository.findById(flashCard.getId()).orElseThrow();
        double updatedAgainEasiness = updatedAgain.getEasiness();

        // Assert
        assertThat(updatedAgainEasiness).isLessThan(updatedEasiness);
    }

    // Допоміжний метод для створення флешкарток
    private FlashCard createFlashCard(String conceptName, String description, Instant dueReview) {
        Concept concept = new Concept();
        concept.setName(conceptName);
        concept.setDescription(description);
        concept.setIsFlashCard(true);
        concept.setCompendium(testCompendium);

        FlashCard flashCard = new FlashCard();
        flashCard.setDueReview(dueReview);

        concept.setFlashCard(flashCard);
        flashCard.setConcept(concept);

        conceptRepository.save(concept);
        flashCardRepository.save(flashCard);

        return flashCard;
    }
}
