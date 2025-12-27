package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.*;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CoursePlanRepository coursePlanRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ConceptRepository conceptRepository;
    @Mock private CompendiumRepository compendiumRepository;
    @Mock private BasicMapper basicMapper;
    @Mock private CacheManager cacheManager;
    @Mock private FlashCardRepository flashCardRepository;

    @InjectMocks
    private CourseServiceImpl service;

    private UserProfile testUser;

    @BeforeEach
    public void setUp() {
        testUser = new UserProfile();
        testUser.setId(1488L);
        testUser.setUsername("test name");
        // ... ініціалізація інших полів
    }

    @Test
    void getCourseById_Success() {
        long courseId = 1L;
        Course course = new Course();
        CourseDetailedDto expectedDto = new CourseDetailedDto(null, null, null, null, null);

        when(courseRepository.findByStudentAndCoursePlanIdWithFetch(testUser.getId(), courseId))
                .thenReturn(Optional.of(course));
        when(basicMapper.courseToDetailedDto(course)).thenReturn(expectedDto);

        var result = service.getCourseById(testUser, courseId);

        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void getCourseById_NotFound_ThrowsException() {
        when(courseRepository.findByStudentAndCoursePlanIdWithFetch(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(MyBadRequestException.class, () -> service.getCourseById(testUser, 1L));
    }

    @Test
    void deleteCourseById_Success() {
        long courseId = 1L;
        Long dbId = 10L;

        when(courseRepository.findIdByStudentAndCoursePlanId(testUser, courseId))
                .thenReturn(Optional.of(dbId));

        service.deleteCourseById(testUser, courseId);

        verify(courseRepository).updateCurrentCompendiumById(null, dbId);
        verify(conceptRepository).deleteByCourseId(dbId);
        verify(compendiumRepository).deleteByCourseId(dbId);
        verify(courseRepository).deleteById(dbId);
    }

    @Test
    void beginCourse_AlreadyStarted_ThrowsException() {
        when(courseRepository.existsByStudentAndCoursePlanId(testUser, 1L)).thenReturn(true);

        assertThrows(MyBadRequestException.class, () -> service.beginCourse(1L, testUser));
    }

    @Test
    void beginCourse_Success() {
        Long cpId = 1L;
        CoursePlan cp = new CoursePlan();
        cp.setIsPublic(true);
        cp.setTopics(List.of(new Topic(), new Topic()));
        cp.setAuthor(testUser);

        when(courseRepository.existsByStudentAndCoursePlanId(testUser, cpId)).thenReturn(false);
        when(coursePlanRepository.findByIdWithTopics(cpId)).thenReturn(Optional.of(cp));

        var result = service.beginCourse(cpId, testUser);

        verify(courseRepository).save(any(Course.class));
        assertNotNull(result);
    }

    @Test
    void endCourse_NoActiveCompendium_ThrowsException() {
        Course course = new Course();
        course.setCurrentCompendium(null); // Курс вже завершений або не розпочатий

        when(courseRepository.findByStudentAndCoursePlanId(testUser, 1L))
                .thenReturn(Optional.of(course));

        assertThrows(MyBadRequestException.class, () -> service.endCourse(1L, testUser));
    }

    @Test
    void beginTopic_WrongStatus_ThrowsException() {
        Compendium compendium = new Compendium();
        compendium.setStatus(CompendiumStatus.COMPLETED); // Не можна почати вже завершений

        when(compendiumRepository.findByTopicIdAndStudentWithCourse(anyLong(), eq(testUser)))
                .thenReturn(Optional.of(compendium));

        assertThrows(MyBadRequestException.class, () -> service.beginTopic(1L, testUser, 1L));
    }

    @Test
    void beginTopic_Success_FirstTopic() {
        long topicId = 1L;
        Course course = new Course();
        Compendium compendium = new Compendium();
        compendium.setIndex(0); // Перша тема
        compendium.setStatus(CompendiumStatus.CAN_START);
        compendium.setCourse(course);
        compendium.setTopic(new Topic());

        when(compendiumRepository.findByTopicIdAndStudentWithCourse(topicId, testUser))
                .thenReturn(Optional.of(compendium));
        when(basicMapper.compendiumToDto(any())).thenReturn(null);

        service.beginTopic(topicId, testUser, 1L);

        assertEquals(CompendiumStatus.CURRENT, compendium.getStatus());
        verify(compendiumRepository, atLeastOnce()).save(compendium);
        verify(courseRepository).save(course);
    }

    @Test
    void updateCompendium_NotCurrent_ThrowsException() {
        TopicCompendiumDto dto = new TopicCompendiumDto(1L, "notes", null, null, CompendiumStatus.LOCKED);
        Compendium compendium = new Compendium();
        compendium.setStatus(CompendiumStatus.CAN_START); // Тільки CURRENT можна редагувати

        when(compendiumRepository.findByIdWithConcepts(1L)).thenReturn(Optional.of(compendium));

        assertThrows(MyBadRequestException.class, () -> service.updateCompendium(dto, testUser, 1L));
    }
}