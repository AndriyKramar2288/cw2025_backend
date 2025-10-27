package com.banew.cw2025_backend_core.backend.utils;

import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.coursePlans.CoursePlanBasicDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserRegisterForm;
import com.banew.cw2025_backend_core.backend.entities.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BasicMapper {
    BasicMapper INSTANCE = Mappers.getMapper(BasicMapper.class);

    UserProfileBasicDto userProfileToBasicDto(UserProfile userProfile);
    UserProfile basicDtoToUserProfile(UserProfileBasicDto dto);
    UserProfile registerFormToUserProfile(UserRegisterForm userRegisterForm);

    CoursePlan basicDtoToCoursePlan(CoursePlanBasicDto dto);
    Topic basicDtoToTopic(CoursePlanBasicDto.TopicBasicDto dto);

    CoursePlanBasicDto coursePlanToBasicDto(CoursePlan coursePlan);
    CoursePlanBasicDto.TopicBasicDto topicToBasicDto(Topic topic);

    TopicCompendiumDto compendiumToDto(Compendium compendium);
    CourseBasicDto courseToBasicDto(Course course);
    TopicCompendiumDto.ConceptBasicDto conceptToDto(Concept concept);

    @AfterMapping
    default void linkTopics(@MappingTarget CoursePlan coursePlan) {
        if (coursePlan.getTopics() != null) {
            coursePlan.getTopics().forEach(topic -> topic.setCoursePlan(coursePlan));
        }
    }
}