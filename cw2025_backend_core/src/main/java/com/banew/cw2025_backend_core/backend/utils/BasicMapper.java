package com.banew.cw2025_backend_core.backend.utils;

import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.CoursePlanCourseDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileCoursePlanDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileDetailedDto;
import com.banew.cw2025_backend_common.dto.users.UserRegisterForm;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.repo.CompendiumRepository;
import com.banew.cw2025_backend_core.backend.repo.ConceptRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface BasicMapper {
    BasicMapper INSTANCE = Mappers.getMapper(BasicMapper.class);

    UserProfileBasicDto userProfileToBasicDto(UserProfile userProfile);
    UserProfile basicDtoToUserProfile(UserProfileBasicDto dto);
    UserProfile registerFormToUserProfile(UserRegisterForm userRegisterForm);

    UserProfileCoursePlanDto coursePlanToUserProfileDetailedDto(CoursePlan coursePlan);
    UserProfileDetailedDto userProfileToDetailedDto(UserProfile userProfile);

    CoursePlan basicDtoToCoursePlan(CoursePlanBasicDto dto);
    Topic basicDtoToTopic(CoursePlanBasicDto.TopicBasicDto dto);

    CoursePlanBasicDto coursePlanToBasicDto(CoursePlan coursePlan);
    CoursePlanBasicDto.TopicBasicDto topicToBasicDto(Topic topic);

    CoursePlanCourseDto coursePlanToCourseDto(CoursePlan coursePlan);
    TopicCompendiumDto compendiumToDto(Compendium compendium);
    @Mapping(source = "currentCompendium.id", target = "currentCompendiumId")
    CourseDetailedDto courseToDetailedDto(Course course);
    TopicCompendiumDto.ConceptBasicDto conceptToDto(Concept concept);

    default CourseBasicDto courseToBasicDto(Course course,
                                            CompendiumRepository compendiumRepository,
                                            ConceptRepository conceptRepository) {
        Optional<Compendium> compendium =
                Optional.ofNullable(course.getCurrentCompendium());

        String topicName = compendium.stream()
                .map(c -> c.getTopic().getName())
                .findFirst().orElse(null);

        return new CourseBasicDto(
                course.getId(), course.getStartedAt(), coursePlanToCourseDto(course.getCoursePlan()),
                topicName,
                conceptRepository.countByCompendium_Course(course),
                compendiumRepository.countByCourse(course)
        );
    }

    @AfterMapping
    default void linkTopics(@MappingTarget CoursePlan coursePlan) {
        if (coursePlan.getTopics() != null) {
            coursePlan.getTopics().forEach(topic -> topic.setCoursePlan(coursePlan));
        }
    }
}