package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.CourseRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CoursePlanServiceImpl implements CoursePlanService {

    private final CoursePlanRepository coursePlanRepository;
    private final BasicMapper basicMapper;
    private final CacheManager cacheManager;
    private final CourseRepository courseRepository;

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
    @Caching(
            put = @CachePut(value = "coursePlans", key = "#id"),
            evict = {
                    @CacheEvict(value = "userProfileDetailedById", key = "#currentUser.id"),
                    @CacheEvict(value = "courses", key = "#currentUser.id")
            }
    )
    @Transactional
    public CoursePlanBasicDto updateCoursePlan(UserProfile currentUser, Long id, CoursePlanBasicDto dto) {
        CoursePlan existingPlan = coursePlanRepository.findByIdWithTopics(id)
                .orElseThrow(() -> new MyBadRequestException("Course with this ID was not found!"));

        if (!existingPlan.getAuthor().getId().equals(currentUser.getId()))
            throw new MyBadRequestException("Course with this ID is not yours!");

        if (dto.name() != null) existingPlan.setName(dto.name());
        if (dto.description() != null) existingPlan.setDescription(dto.description());
        if (dto.backgroundSrc() != null) existingPlan.setBackgroundSrc(dto.backgroundSrc());
        if (dto.isPublic() != null) existingPlan.setIsPublic(dto.isPublic());

        existingPlan.getTopics().forEach(t -> {
            dto.topics().forEach(topicBasicDto -> {
                if (topicBasicDto.id().equals(t.getId())) {
                    if (topicBasicDto.name() != null) t.setName(topicBasicDto.name());
                    if (topicBasicDto.description() != null) t.setDescription(topicBasicDto.description());
                }
            });
        });

        var cache = cacheManager.getCache("courseById");
        if (cache != null) courseRepository.findByCoursePlanIdWithStudent(id).forEach(c -> {
            cache.evict(id + "_" + c.getStudent().getId());
        });

        coursePlanRepository.save(existingPlan);
        return basicMapper.coursePlanToBasicDto(existingPlan);
    }

    @Override
    public List<CoursePlanBasicDto> getAllExistingPlans(UserProfile currentUser) {
        return coursePlanRepository.findCoursesForBasicDto().stream()
                .filter(cp -> cp.getIsPublic() || cp.getAuthor().getId().equals(currentUser.getId()))
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
    }

    @Override
    public List<CoursePlanBasicDto> getPlansBySearchQuery(UserProfile currentUser, String query) {
        return (query == null || query.isEmpty()
                ? coursePlanRepository.findCoursesForBasicDto(Pageable.ofSize(10))
                : coursePlanRepository.findByText(query)
                ).stream()
                .filter(cp -> cp.getIsPublic() || cp.getAuthor().getId().equals(currentUser.getId()))
                .map(basicMapper::coursePlanToBasicDto)
                .toList();
    }

    @Override
    @Cacheable(value = "coursePlans", key = "#id")
    public CoursePlanBasicDto getCoursePlanById(UserProfile currentUser, Long id) {
        return basicMapper.coursePlanToBasicDto(
                coursePlanRepository.findByIdWithTopics(id)
                        .filter(cp -> cp.getIsPublic() || cp.getAuthor().getId().equals(currentUser.getId()))
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
