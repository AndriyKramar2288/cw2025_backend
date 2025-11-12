package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.*;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CoursePlanServiceIntegrationTest {

    @Autowired
    private CoursePlanService coursePlanService;

    @Autowired
    private CoursePlanRepository coursePlanRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CompendiumRepository compendiumRepository;

    @BeforeEach
    void cleanDB() {
        compendiumRepository.deleteAll();
        courseRepository.deleteAll();
        topicRepository.deleteAll();
        coursePlanRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @Test
    void createCoursePlan_withTopics_savesCorrectly() {
        // Given
        var dto = createSampleCoursePlanDto();
        var user = createAndSaveUser("John Doe", "aboba@gmail.com", "qweqwr343r");

        // When
        CoursePlanBasicDto result = coursePlanService.createCoursePlan(user, dto);

        // Then
        assertNotNull(result);
        assertEquals(2, result.topics().size());

        // Перевіряємо в БД
        List<CoursePlan> plans = coursePlanRepository.findAll();
        assertEquals(1, plans.size());

        CoursePlan savedPlan = plans.getFirst();
        assertEquals("Java Basics", savedPlan.getName());
        assertEquals(2, savedPlan.getTopics().size());

        // Перевіряємо зворотний зв'язок
        savedPlan.getTopics().forEach(topic ->
                assertNotNull(topic.getCoursePlan())
        );
    }

    @Test
    void updateCoursePlan_withTopics_savesCorrectly() {
        // Given
        var dto = createSampleCoursePlanDto();
        var user = createAndSaveUser("John Doe", "aboba@gmail.com", "qweqwr343r");

        CoursePlanBasicDto result = coursePlanService.createCoursePlan(user, dto);

        // When
        var result2 = coursePlanService.updateCoursePlan(user, new CoursePlanBasicDto(
                result.id(),
                "BBCZZ",
                result.author(),
                "ABBSEWQ",
                List.of(
                        new CoursePlanBasicDto.TopicBasicDto(
                                result.topics().get(0).id(),
                                "Loops forever",
                                "Learn about abobas"
                        ),
                        result.topics().get(1)
                )
        ));

        // Then
        assertEquals("BBCZZ", result2.name());
        assertEquals("ABBSEWQ", result2.description());
        assertNotNull(result2);
        assertEquals(2, result.topics().size());

        CoursePlanBasicDto.TopicBasicDto topicBasicDto = result2.topics().getFirst();
        assertEquals("Loops forever", topicBasicDto.name());

        // Given 2
        var user2 = createAndSaveUser("John Pidoras", "aboba1588@mgail.com", "wqeqweqwq");

        // When / Then
        var ex = assertThrows(MyBadRequestException.class, () -> coursePlanService.updateCoursePlan(user2, result2));
        assertEquals("Course with this ID is not yours!", ex.getMessage());
    }

    private CoursePlanBasicDto createSampleCoursePlanDto() {
        CoursePlanBasicDto.TopicBasicDto topic1 = new CoursePlanBasicDto.TopicBasicDto(null,
                "Variables",
                "Learn about variables");

        CoursePlanBasicDto.TopicBasicDto topic2 = new CoursePlanBasicDto.TopicBasicDto(null,
                "Loops",
                "Learn about loops");


        return new CoursePlanBasicDto(null,
                "Java Basics",
                null,
                "Learn Java from scratch",
                List.of(topic1, topic2));
    }

    private UserProfile createAndSaveUser(String username, String email, String password) {
        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return userProfileRepository.save(user);
    }
}
