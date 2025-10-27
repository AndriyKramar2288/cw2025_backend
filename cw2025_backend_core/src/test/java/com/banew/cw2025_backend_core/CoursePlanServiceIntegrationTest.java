package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
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

    @BeforeEach
    void cleanDB() {
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
        assertEquals(2, result.getTopics().size());

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

        result.setName("BBCZZ");
        result.setDescription("ABBSEWQ");

        CoursePlanBasicDto.TopicBasicDto topic1changed = result.getTopics().getFirst();
        topic1changed.setName("Loops forever");
        topic1changed.setDescription("Learn about abobas");

        // When
        var result2 = coursePlanService.updateCoursePlan(user, result);

        // Then
        assertEquals("BBCZZ", result2.getName());
        assertEquals("ABBSEWQ", result2.getDescription());
        assertNotNull(result2);
        assertEquals(2, result.getTopics().size());

        CoursePlanBasicDto.TopicBasicDto topicBasicDto = result2.getTopics().getFirst();
        assertEquals("Loops forever", topicBasicDto.getName());

        // Given 2
        var user2 = createAndSaveUser("John Pidoras", "aboba1588@mgail.com", "wqeqweqwq");

        // When / Then
        var ex = assertThrows(MyBadRequestException.class, () -> coursePlanService.updateCoursePlan(user2, result2));
        assertEquals("Course with this ID is not yours!", ex.getMessage());
    }

    private CoursePlanBasicDto createSampleCoursePlanDto() {
        CoursePlanBasicDto dto = new CoursePlanBasicDto();
        dto.setName("Java Basics");
        dto.setDescription("Learn Java from scratch");

        CoursePlanBasicDto.TopicBasicDto topic1 = new CoursePlanBasicDto.TopicBasicDto();
        topic1.setName("Variables");
        topic1.setDescription("Learn about variables");

        CoursePlanBasicDto.TopicBasicDto topic2 = new CoursePlanBasicDto.TopicBasicDto();
        topic2.setName("Loops");
        topic2.setDescription("Learn about loops");

        dto.setTopics(List.of(topic1, topic2));
        return dto;
    }

    private UserProfile createAndSaveUser(String username, String email, String password) {
        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return userProfileRepository.save(user);
    }
}
