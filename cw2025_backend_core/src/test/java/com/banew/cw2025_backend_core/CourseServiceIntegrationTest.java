package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.*;
import com.banew.cw2025_backend_core.backend.services.interfaces.CourseService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourseServiceIntegrationTest {

    @Autowired
    private CourseService courseService;

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
        topicRepository.deleteAll();
        courseRepository.deleteAll();
        coursePlanRepository.deleteAll();
        compendiumRepository.deleteAll();
        userProfileRepository.deleteAll();

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
    void beginCourse_validCoursePlan_createsCompendiumsForAllTopics() {
        // When
        CourseBasicDto result = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // Then
        assertNotNull(result);
        assertNotNull(result.startedAt());
        assertEquals(3, result.compendiums().size());

        // Перевіряємо в БД
        List<Course> courses = courseRepository.findByStudent(testUser);
        assertEquals(1, courses.size());

        Course savedCourse = courses.get(0);
        assertEquals(3, savedCourse.getCompendiums().size());
        assertNull(savedCourse.getCurrentCompendiumId()); // ще не почали жодного топіку

        // Перевіряємо індекси компендіумів
        assertEquals(0, savedCourse.getCompendiums().get(0).getIndex());
        assertEquals(1, savedCourse.getCompendiums().get(1).getIndex());
        assertEquals(2, savedCourse.getCompendiums().get(2).getIndex());
    }

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

    @Test
    void beginCourse_compendiumsHaveCorrectTopics() {
        // When
        CourseBasicDto result = courseService.beginCourse(testCoursePlan.getId(), testUser);

        // Then
        assertEquals("Variables", result.compendiums().get(0).topic().name());
        assertEquals("Loops", result.compendiums().get(1).topic().name());
        assertEquals("OOP", result.compendiums().get(2).topic().name());
    }

    // ========== BEGIN TOPIC TESTS ==========

    @Test
    void beginTopic_firstTopic_setsCurrentCompendium() {
        // Given - починаємо курс
        courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When - починаємо перший топік
        TopicCompendiumDto result = courseService.beginTopic(topic1.getId(), testUser);

        // Then
        assertNotNull(result);
        assertEquals("Variables", result.topic().name());

        // Перевіряємо, що currentCompendiumId встановлено
        List<Course> courses = courseRepository.findByStudent(testUser);
        assertNotNull(courses.get(0).getCurrentCompendiumId());
    }

    @Test
    void beginTopic_secondTopicWithoutFirst_throwsException() {
        // Given - починаємо курс
        courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When & Then - пробуємо почати другий топік без першого
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(topic2.getId(), testUser)
        );
    }

    @Test
    void beginTopic_sequentialTopics_worksCorrectly() {
        // Given - починаємо курс
        courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When - послідовно починаємо топіки
        TopicCompendiumDto result1 = courseService.beginTopic(topic1.getId(), testUser);
        TopicCompendiumDto result2 = courseService.beginTopic(topic2.getId(), testUser);
        TopicCompendiumDto result3 = courseService.beginTopic(topic3.getId(), testUser);

        // Then
        assertEquals("Variables", result1.topic().name());
        assertEquals("Loops", result2.topic().name());
        assertEquals("OOP", result3.topic().name());
    }

    @Test
    void beginTopic_skipTopic_throwsException() {
        // Given
        courseService.beginCourse(testCoursePlan.getId(), testUser);
        courseService.beginTopic(topic1.getId(), testUser);

        // When & Then - пробуємо пропустити topic2 і почати topic3
        MyBadRequestException exception = assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(topic3.getId(), testUser)
        );

        assertTrue(exception.getMessage().contains("Wrong position"));
    }

    @Test
    void beginTopic_nonExistingTopic_throwsException() {
        // Given
        courseService.beginCourse(testCoursePlan.getId(), testUser);

        // When & Then
        assertThrows(
                MyBadRequestException.class,
                () -> courseService.beginTopic(999L, testUser)
        );
    }

    // ========== UPDATE COMPENDIUM TESTS ==========

    @Test
    void updateCompendium_addNotes_savesCorrectly() {
        // Given
        courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser);

        // When
        TopicCompendiumDto updateDto = new TopicCompendiumDto(
                compendium.id(),
                "These are my notes about variables",
                compendium.topic(),
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto, null);

        // Then
        assertEquals("These are my notes about variables", result.notes());

        // Перевіряємо в БД
        Compendium saved = compendiumRepository.findById(compendium.id()).orElseThrow();
        assertEquals("These are my notes about variables", saved.getNotes());
    }

    @Test
    void updateCompendium_addConcepts_savesCorrectly() {
        // Given
        courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser);

        // When
        var concept1 = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "int",
                "Integer data type"
        );

        var concept2 = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "String",
                "Text data type"
        );

        TopicCompendiumDto updateDto = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(concept1, concept2),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto, null);

        // Then
        assertEquals(2, result.concepts().size());

        // Перевіряємо в БД
        Compendium saved = compendiumRepository.findById(compendium.id()).orElseThrow();
        assertEquals(2, saved.getConcepts().size());
    }

    @Test
    void updateCompendium_updateExistingConcept_modifiesCorrectly() {
        // Given - додаємо концепт
        courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser);

        var concept = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "int",
                "Old description"
        );

        TopicCompendiumDto updateDto1 = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(concept),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto saved = courseService.updateCompendium(updateDto1, null);
        Long conceptId = saved.concepts().get(0).id();

        // When - оновлюємо існуючий концепт
        var updatedConcept = new TopicCompendiumDto.ConceptBasicDto(
                conceptId,
                "int",
                "New description"
        );

        TopicCompendiumDto updateDto2 = new TopicCompendiumDto(
                compendium.id(),
                compendium.notes(),
                compendium.topic(),
                List.of(updatedConcept),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto2, null);

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
                () -> courseService.updateCompendium(updateDto, null)
        );

        assertTrue(exception.getMessage().contains("is no exists"));
    }

    @Test
    void updateCompendium_nullFields_keepsOldValues() {
        // Given
        courseService.beginCourse(testCoursePlan.getId(), testUser);
        TopicCompendiumDto compendium = courseService.beginTopic(topic1.getId(), testUser);

        // Додаємо нотатки
        TopicCompendiumDto updateDto1 = new TopicCompendiumDto(
                compendium.id(),
                "Original notes",
                compendium.topic(),
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );
        courseService.updateCompendium(updateDto1, null);

        // When - оновлюємо без нотаток
        TopicCompendiumDto updateDto2 = new TopicCompendiumDto(
                compendium.id(),
                null,
                compendium.topic(),// явно null
                compendium.concepts(),
                CompendiumStatus.LOCKED
        );

        TopicCompendiumDto result = courseService.updateCompendium(updateDto2, null);

        // Then - нотатки залишились
        assertEquals("Original notes", result.notes());
    }

    // ========== FULL FLOW TEST ==========

    @Test
    void fullCourseFlow_beginToCompletion_worksCorrectly() {
        // 1. Почати курс
        CourseBasicDto course = courseService.beginCourse(testCoursePlan.getId(), testUser);
        assertNotNull(course);
        assertEquals(3, course.compendiums().size());

        // 2. Почати перший топік
        TopicCompendiumDto compendium1 = courseService.beginTopic(topic1.getId(), testUser);
        assertEquals("Variables", compendium1.topic().name());

        // 3. Додати нотатки до першого топіку
        TopicCompendiumDto updateDto1 = new TopicCompendiumDto(
                compendium1.id(),
                "Variables notes",
                compendium1.topic(),
                compendium1.concepts(),
                CompendiumStatus.LOCKED
        );
        courseService.updateCompendium(updateDto1, null);

        // 4. Почати другий топік
        TopicCompendiumDto compendium2 = courseService.beginTopic(topic2.getId(), testUser);
        assertEquals("Loops", compendium2.topic().name());

        // 5. Додати концепти до другого топіку
        var concept = new TopicCompendiumDto.ConceptBasicDto(
                null,
                "for loop",
                "Iterative loop"
        );

        TopicCompendiumDto updateDto2 = new TopicCompendiumDto(
                compendium2.id(),
                compendium2.notes(),
                compendium2.topic(),
                List.of(concept),
                CompendiumStatus.LOCKED
        );
        TopicCompendiumDto updated = courseService.updateCompendium(updateDto2, null);

        assertEquals(1, updated.concepts().size());

        // 6. Перевірити, що все збереглося
        List<CourseBasicDto> userCourses = courseService.getUserCourses(testUser);
        assertEquals(1, userCourses.size());

        CourseBasicDto savedCourse = userCourses.get(0);
        assertEquals("Variables notes", savedCourse.compendiums().get(0).notes());
        assertEquals(1, savedCourse.compendiums().get(1).concepts().size());
    }
}
