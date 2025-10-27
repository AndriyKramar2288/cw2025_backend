package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_core.backend.entities.CoursePlan;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.TopicRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.CoursePlanService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CoursePlanServiceImpl implements CoursePlanService {

    private CoursePlanRepository coursePlanRepository;
    private TopicRepository topicRepository;
    private BasicMapper basicMapper;

    @Override
    public CoursePlanBasicDto createCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto) {
        CoursePlan coursePlan = basicMapper.basicDtoToCoursePlan(dto);
        coursePlan.setAuthor(currentUser);
        coursePlanRepository.save(coursePlan);
        return basicMapper.coursePlanToBasicDto(coursePlan);
    }

    @Override
    public CoursePlanBasicDto updateCoursePlan(UserProfile currentUser, CoursePlanBasicDto dto) {
        CoursePlan existingPlan = coursePlanRepository.findById(dto.getId())
                .orElseThrow(() -> new MyBadRequestException("Course with this ID was not found!"));

        if (!existingPlan.getAuthor().getId().equals(currentUser.getId()))
            throw new MyBadRequestException("Course with this ID is not yours!");

        if (dto.getName() != null) existingPlan.setName(dto.getName());
        if (dto.getDescription() != null) existingPlan.setDescription(dto.getDescription());

        existingPlan.getTopics().forEach(t -> {
            dto.getTopics().forEach(topicBasicDto -> {
                if (topicBasicDto.getId().equals(t.getId())) {
                    if (topicBasicDto.getName() != null) t.setName(topicBasicDto.getName());
                    if (topicBasicDto.getDescription() != null) t.setDescription(topicBasicDto.getDescription());
                }
            });
        });

        coursePlanRepository.save(existingPlan);
        return basicMapper.coursePlanToBasicDto(existingPlan);
    }
}
