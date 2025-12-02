package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.TopicRepository;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CoursePlanServiceImpl implements CoursePlanService {

    private final CoursePlanRepository coursePlanRepository;
    private final TopicRepository topicRepository;
    private final BasicMapper basicMapper;
    private final UserProfileRepository userProfileRepository;
    private final CacheManager cacheManager;

    @Override
    @CachePut(value = "coursePlans", key = "#result.id")
    @CacheEvict(value = "userProfileDetailedById", key = "#currentUser.id")
    @Transactional
    public CoursePlanBasicDto createCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto) {
        CoursePlan coursePlan = basicMapper.basicDtoToCoursePlan(dto);
        coursePlan.setAuthor(currentUser);
        coursePlanRepository.save(coursePlan);
        return basicMapper.coursePlanToBasicDto(coursePlan);
    }

    @Override
    @CachePut(value = "coursePlans", key = "#result.id")
    @CacheEvict(value = "userProfileDetailedById", key = "#currentUser.id")
    @Transactional
    public CoursePlanBasicDto updateCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto) {
        CoursePlan existingPlan = coursePlanRepository.findByIdWithTopics(dto.id())
                .orElseThrow(() -> new MyBadRequestException("Course with this ID was not found!"));

        if (!existingPlan.getAuthor().getId().equals(currentUser.getId()))
            throw new MyBadRequestException("Course with this ID is not yours!");

        if (dto.name() != null) existingPlan.setName(dto.name());
        if (dto.description() != null) existingPlan.setDescription(dto.description());

        existingPlan.getTopics().forEach(t -> {
            dto.topics().forEach(topicBasicDto -> {
                if (topicBasicDto.id().equals(t.getId())) {
                    if (topicBasicDto.name() != null) t.setName(topicBasicDto.name());
                    if (topicBasicDto.description() != null) t.setDescription(topicBasicDto.description());
                }
            });
        });

        coursePlanRepository.save(existingPlan);
        return basicMapper.coursePlanToBasicDto(existingPlan);
    }

    @Override
    public List<CoursePlanBasicDto> getAllExistingPlans() {
        return coursePlanRepository.findCoursesForBasicDto().stream()
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
    }

    @Override
    public List<CoursePlanBasicDto> getPlansBySearchQuery(String query) {
        return (query == null || query.isEmpty()
                ? coursePlanRepository.findCoursesByIdForBasicDto(
                    coursePlanRepository.findPopularCoursePlanIds(Pageable.ofSize(10)))
                : coursePlanRepository.findByText(query)
                ).stream()
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
    }

    @Override
    @Cacheable(value = "coursePlans", key = "#id")
    public CoursePlanBasicDto getCoursePlanById(Long id) {
        return basicMapper.coursePlanToBasicDto(
                coursePlanRepository.findByIdWithTopics(id)
                        .orElseThrow(() -> new MyBadRequestException("Course with this ID was not found!"))
        );
    }

    @Override
    public void evictByAuthorId(Long authorId) {
        Cache cache = cacheManager.getCache("coursePlans");
        if (cache != null) {
            coursePlanRepository.findIdByAuthorId(authorId).forEach(cache::evict);
        }
    }
}
