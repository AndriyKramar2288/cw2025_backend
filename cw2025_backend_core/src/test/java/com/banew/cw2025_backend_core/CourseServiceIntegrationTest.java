package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.*;
import com.banew.cw2025_backend_core.backend.services.implementations.CourseServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CourseServiceIntegrationTest {

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CoursePlanRepository coursePlanRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CompendiumRepository compendiumRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UserProfile testUser;
    private CoursePlan testCoursePlan;
    private Topic topic1;
    private Topic topic2;
    private Topic topic3;

    @BeforeEach
    void setUp() {
        // Створюємо тестового користувача
        testUser = new UserProfile();
        testUser.setEmail("student@test.com");
        testUser.setUsername("Test Student");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(List.of("USER"));
        testUser = userProfileRepository.save(testUser);

        // Створюємо CoursePlan з топіками
        testCoursePlan = new CoursePlan();
        testCoursePlan.setName("Java Fundamentals");
        testCoursePlan.setDescription("Learn Java basics");
        testCoursePlan = coursePlanRepository.save(testCoursePlan);

        // Створюємо топіки
        topic1 = new Topic();
        topic1.setName("Variables");
        topic1.setDescription("Learn about variables");
        topic1.setCoursePlan(testCoursePlan);

        topic2 = new Topic();
        topic2.setName("Loops");
        topic2.setDescription("Learn about loops");
        topic2.setCoursePlan(testCoursePlan);

        topic3 = new Topic();
        topic3.setName("OOP");
        topic3.setDescription("Object-oriented programming");
        topic3.setCoursePlan(testCoursePlan);

        topic1 = topicRepository.save(topic1);
        topic2 = topicRepository.save(topic2);
        topic3 = topicRepository.save(topic3);

        testCoursePlan.setTopics(new ArrayList<>(List.of(topic1, topic2, topic3)));
        testCoursePlan = coursePlanRepository.save(testCoursePlan);
    }

    // ========== GET USER COURSES TESTS ==========

    @Test
    void getUserCourses_noCourses_returnsEmptyList() {
        // When
        List<CourseBasicDto> courses = courseService.getUserCourses(testUser);

        // Then
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    @Test
    void getUserCourses_withCourses_returnsCourseList() {
        // Given - починаємо курс
        courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When
        List<CourseBasicDto> courses = courseService.getUserCourses(testUser);

        // Then
        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertEquals("Java Fundamentals", courses.get(0).coursePlan().name());
    }

    // ========== BEGIN COURSE TESTS ==========

    @Test
    void beginCourse_nonExistingCoursePlan_throwsException() {
        // When & Then
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginCourse(999L, testUser)
        );

        assertTrue(exception.getMessage().contains("is no exists"));
        assertEquals(0, courseRepository.findByStudent(testUser).size());
    }

    // ========== BEGIN TOPIC TESTS ==========

    @Test
    void beginTopic_firstTopic_setsCurrentCompendium() {
        // Given - починаємо курс
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When - починаємо перший топік
        TopicCompendiumDto result = courseService.beginTopic(topic1.getId(), testUser, crs.id());

        // Then
        assertNotNull(result);
        assertEquals("Variables", result.topic().name());

        // Перевіряємо, що currentCompendiumId встановлено
        List<Course> courses = courseRepository.findByStudent(testUser);
        assertNotNull(courses.get(0).getCurrentCompendium());
    }

    @Test
    void beginTopic_secondTopicWithoutFirst_throwsException() {
        // Given - починаємо курс
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When & Then - пробуємо почати другий топік без першого
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(topic2.getId(), testUser, crs.id())
        );
    }

    @Test
    void beginTopic_sequentialTopics_worksCorrectly() {
        // Given - починаємо курс
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When - послідовно починаємо топіки
        TopicCompendiumDto result1 = courseService.beginTopic(topic1.getId(), testUser, crs.id());
        TopicCompendiumDto result2 = courseService.beginTopic(topic2.getId(), testUser, crs.id());
        TopicCompendiumDto result3 = courseService.beginTopic(topic3.getId(), testUser, crs.id());

        // Then
        assertEquals("Variables", result1.topic().name());
        assertEquals("Loops", result2.topic().name());
        assertEquals("OOP", result3.topic().name());
    }

    @Test
    void beginTopic_skipTopic_throwsException() {
        // Given
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);
        courseService.beginTopic(topic1.getId(), testUser, crs.id());

        // When & Then - пробуємо пропустити topic2 і почати topic3
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(topic3.getId(), testUser, crs.id())
        );

        assertTrue(exception.getMessage().contains("Wrong position"));
    }

    @Test
    void beginTopic_nonExistingTopic_throwsException() {
        // Given
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When & Then
        assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(999L, testUser, crs.id())
        );
    }

    // ========== UPDATE COMPENDIUM TESTS ==========

    @Test
    void updateCompendium_addNotes_savesCorrectly() {
        // Given
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser, crs.id());

        // When
        TopicCompendiumDto updateDto = new TopicCompendiumDto(
                compendium.id(),
                "These are my notes about variables",
                compendium.topic(),
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto, testUser, crs.id());

        // Then
        assertEquals("These are my notes about variables", result.notes());

        // Перевіряємо в БД
        Compendium saved = compendiumRepository.findById(compendium.id()).orElseThrow();
        assertEquals("These are my notes about variables", saved.getNotes());
    }

    @Test
    void updateCompendium_addConcepts_savesCorrectly() {
        // Given
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser, crs.id());

        // When
        var concept1 = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "int",
                "Integer data type", false
        );

        var concept2 = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "String",
                "Text data type", false
        );

        TopicCompendiumDto updateDto = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(concept1, concept2),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto, testUser, crs.id());

        // Then
        assertEquals(2, result.concepts().size());

        // Перевіряємо в БД
        Compendium saved = compendiumRepository.findById(compendium.id()).orElseThrow();
        assertEquals(2, saved.getConcepts().size());
    }

    @Test
    void updateCompendium_updateExistingConcept_modifiesCorrectly() {
        // Given - додаємо концепт
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser, crs.id());

        var concept = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "int",
                "Old description", false
        );

        TopicCompendiumDto updateDto1 = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(concept),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto saved = courseService.updateCompendium(updateDto1, testUser, crs.id());
        Long conceptId = saved.concepts().get(0).id();

        // When - оновлюємо існуючий концепт
        var updatedConcept = new TopicCompendiumDto.ConceptBasicDto(
                conceptId,
                "int",
                "New description", false
        );

        TopicCompendiumDto updateDto2 = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(updatedConcept),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto2, testUser, crs.id());

        // Then
        assertEquals("New description", result.concepts().get(0).description());
    }

    @Test
    void updateCompendium_nonExistingCompendium_throwsException() {
        // Given
        TopicCompendiumDto updateDto = new TopicCompendiumDto(
                999L,
                "Some notes",
                null,
                null,
                CompendiumStatus.LOCKED
        );

        // When & Then
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.updateCompendium(updateDto, testUser, 5L)
        );

        assertTrue(exception.getMessage().contains("is no exists"));
    }

    @Test
    void updateCompendium_nullFields_keepsOldValues() {
        // Given
        var crs = courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser, crs.id());

        // Додаємо нотатки
        TopicCompendiumDto updateDto1 = new TopicCompendiumDto(
                compendium.id(),
                "Original notes",
                compendium.topic(),
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );
        courseService.updateCompendium(updateDto1, testUser, crs.id());

        // When - оновлюємо без нотаток
        TopicCompendiumDto updateDto2 = new TopicCompendiumDto(
                compendium.id(),
                null,
                compendium.topic(),// явно null
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto2, testUser, crs.id());

        // Then - нотатки залишились
        assertEquals("Original notes", result.notes());
    }
}
