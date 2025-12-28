package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.Topic;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.CourseRepository;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursePlanServiceImplTest {

    @Mock
    private CoursePlanRepository coursePlanRepository;
    @Spy
    private BasicMapper basicMapper = BasicMapper.INSTANCE;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private CoursePlanServiceImpl service;

    private UserProfile user;

    @BeforeEach
    void setUp() {
        user = Instancio.of(UserProfile.class)
                .set(Select.field(UserProfile::getCoursePlans), Instancio
                        .ofSet(CoursePlan.class)
                        .size(5)
                        .create())
                .create();
        user.getCoursePlans().forEach(cp -> cp.setAuthor(user));
    }

    @Test
    void createCoursePlan_TryCreateNewCoursePlan_Success() {
        // given
        var cpForm = Instancio
                .of(CoursePlanBasicDto.class)
                .set(Select.field(CoursePlanBasicDto::id), null)
                .set(Select.field(CoursePlanBasicDto::author), null)
                .create();
        var prevUserCpCount = user.getCoursePlans().size();
        // when
        when(coursePlanRepository.save(any())).thenAnswer((i) -> {
            var cp = (CoursePlan) i.getArgument(0);
            cp.setId(Instancio.create(Long.class));
            cp.getAuthor().getCoursePlans().add(cp);
            return cp;
        });
        // then
        var r = service.createCoursePlan(user, cpForm);
        assertNotNull(r);
        assertEquals(basicMapper.userProfileToBasicDto(user), r.author());
        assertNotNull(r.id());
        assertEquals(prevUserCpCount + 1, user.getCoursePlans().size());
        verify(coursePlanRepository).save(any());
    }

    @Test
    void updateCoursePlan_TryUpdateCoursePlan_SuccessResult() {
        // given
        var cp = Instancio.of(CoursePlan.class)
                .set(Select.field(CoursePlan::getAuthor), user)
                .set(Select.field(CoursePlan::getTopics), Instancio.ofList(Topic.class)
                        .size(5)
                        .create())
                .create();
        cp.getTopics().forEach(t -> t.setCoursePlan(cp));
        user.getCoursePlans().add(cp);
        var topicToChange = cp.getTopics().getFirst(); // topic, which we will update
        topicToChange.setName(Instancio.create(String.class));
        topicToChange.setDescription(Instancio.create(String.class));
        var newCpForm = Instancio.of(CoursePlanBasicDto.class)
                .set(Select.field(CoursePlanBasicDto::id), cp.getId())
                .create();
        newCpForm.topics().add(basicMapper.topicToBasicDto(topicToChange));
        // when
        when(coursePlanRepository.findByIdWithTopics(cp.getId())).thenReturn(Optional.of(cp));
        when(coursePlanRepository.save(any())).thenAnswer((i) -> i.getArgument(0));
        // then
        var r = service.updateCoursePlan(user, cp.getId(), newCpForm);
        assertEquals(newCpForm.name(), r.name());
        assertEquals(newCpForm.backgroundSrc(), r.backgroundSrc());
        assertEquals(newCpForm.isPublic(), r.isPublic());
        assertEquals(basicMapper.userProfileToBasicDto(user), r.author());
        assertTrue(r.topics().stream().anyMatch(t -> t.equals(basicMapper.topicToBasicDto(topicToChange))));
        verify(coursePlanRepository).save(any());
    }

    @Test
    void updateCoursePlan_TryUpdateForeignCoursePlan_Failure() {
        // given
        var foreignCoursePlan = Instancio.create(CoursePlan.class);
        // when
        when(coursePlanRepository.findByIdWithTopics(foreignCoursePlan.getId()))
                .thenReturn(Optional.of(foreignCoursePlan));
        // then
        assertNotEquals(user.getId(), foreignCoursePlan.getAuthor().getId());
        var ex = assertThrows(MyBadRequestException.class,
                () -> service.updateCoursePlan(user,
                        foreignCoursePlan.getId(),
                        Instancio.create(CoursePlanBasicDto.class)));
        assertEquals("Course with this ID is not yours!", ex.getMessage().trim());
        verify(coursePlanRepository).findByIdWithTopics(foreignCoursePlan.getId());
    }

    @Test
    void updateCoursePlan_TryUpdateNotExistingCoursePlan_Failure() {
        // when
        when(coursePlanRepository.findByIdWithTopics(any())).thenReturn(Optional.empty());
        // then
        var ex = assertThrows(MyBadRequestException.class,
                () -> service.updateCoursePlan(user,
                        Instancio.create(Long.class),
                        Instancio.create(CoursePlanBasicDto.class)));
        assertEquals("Course with this ID was not found!", ex.getMessage().trim());
        verify(coursePlanRepository).findByIdWithTopics(any());
    }

    @Test
    void getAllExistingPlans_DefaultCall_ValidResult() {
        // given
        List<CoursePlan> coursePlanDbList = Stream.concat(Instancio.ofList(CoursePlan.class)
            .size(20).create().stream(), user.getCoursePlans().stream()).toList();
        var expectedResult = coursePlanDbList.stream()
                .filter(cp -> cp.getIsPublic() || user.getId().equals(cp.getAuthor().getId()))
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
        // when
        when(coursePlanRepository.findCoursesForBasicDto()).thenReturn(coursePlanDbList);
        // then
        var r = service.getAllExistingPlans(user);
        assertEquals(expectedResult.size(), r.size());
        assertEquals(expectedResult, r);
    }

    @Test
    void getPlansBySearchQuery_nullQuery_PopularCourses() {
        // given
        final int SIZE = CoursePlanServiceImpl.PAGE_SIZE;
        String query = null;
        List<CoursePlan> coursePlanDbList = Stream.concat(Instancio.ofList(CoursePlan.class)
                    .size(SIZE + 5)
                    .create().stream(), user.getCoursePlans().stream())
                .sorted(Comparator.comparing(CoursePlan::getStudentCount).reversed())
                .limit(SIZE)
                .toList();
        var expectedResult = coursePlanDbList.stream()
                .filter(cp -> cp.getIsPublic() || user.getId().equals(cp.getAuthor().getId()))
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
        // when
        when(coursePlanRepository.findCoursesForBasicDto(Pageable.ofSize(SIZE)))
                .thenReturn(coursePlanDbList);
        //then
        var r = service.getPlansBySearchQuery(user, query);
        assertEquals(expectedResult.size(), r.size());
        assertEquals(expectedResult, r);
    }

    @Test
    void getPlansBySearchQuery_PresentFineQuery_SearchByName() {
        // given
        var cp = user.getCoursePlans().stream().findFirst().orElseThrow();
        String query = cp.getName();
        // when
        when(coursePlanRepository.findByText(query)).thenReturn(List.of(cp));
        // then
        var r = service.getPlansBySearchQuery(user, query);
        var expectedResult = Stream.of(cp).map(basicMapper::coursePlanToBasicDto).toList();
        assertEquals(expectedResult, r);
        verify(coursePlanRepository).findByText(query);
    }

    @Test
    void getPlansBySearchQuery_PresentBadQuery_SearchByName() {
        // given
        var cp = user.getCoursePlans().stream().findFirst().orElseThrow();
        String query = "SEARCH_" + cp.getName();
        // when
        when(coursePlanRepository.findByText(query)).thenReturn(List.of());
        // then
        var r = service.getPlansBySearchQuery(user, query);
        assertEquals(List.of(), r);
        verify(coursePlanRepository).findByText(query);
    }

    @Test
    void getCoursePlanById_OwnCoursePlan_Found() {
        // given
        var cp = user.getCoursePlans().stream().findFirst().orElseThrow();
        // when
        when(coursePlanRepository.findByIdWithTopics(cp.getId())).thenReturn(Optional.of(cp));
        // then
        var r = service.getCoursePlanById(user, cp.getId());
        assertEquals(basicMapper.coursePlanToBasicDto(cp), r);
        verify(coursePlanRepository).findByIdWithTopics(cp.getId());
    }

    @Test
    void getCoursePlanById_ForeignPrivateCoursePlan_NotFound() {
        // given
        var cp = user.getCoursePlans().stream().findFirst().orElseThrow();
        cp.setIsPublic(false);
        var otherUser = Instancio
                .of(UserProfile.class)
                .set(Select.field(UserProfile::getId), user.getId() + 1)
                .create();
        // when
        when(coursePlanRepository.findByIdWithTopics(cp.getId())).thenReturn(Optional.of(cp));
        // then
        var ex = assertThrows(MyBadRequestException.class,
                () -> service.getCoursePlanById(otherUser, cp.getId()));
        assertEquals("Course with this ID was not found!", ex.getMessage().trim());
        verify(coursePlanRepository).findByIdWithTopics(cp.getId());
    }
}