package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.*;
import com.banew.cw2025_backend_core.backend.services.interfaces.CourseService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private TopicRepository topicRepository;
    private CoursePlanRepository coursePlanRepository;
    private CourseRepository courseRepository;
    private ConceptRepository conceptRepository;
    private CompendiumRepository compendiumRepository;
    private BasicMapper basicMapper;

    @Override
    @Transactional
    @Cacheable(value = "courses", key = "#currentUser.id")
    public List<CourseBasicDto> getUserCourses(UserProfile currentUser) {
        return courseRepository.findByStudent(currentUser).stream()
                .map(o -> basicMapper.courseToBasicDto(o, compendiumRepository, conceptRepository))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Cacheable(value = "courseById", key = "#courseId + '_' + #currentUser.id")
    public CourseDetailedDto getCourseById(UserProfile currentUser, Long courseId) {
        var ex = new MyBadRequestException(
                "CoursePlan with id '" + courseId + "' is no exists!"
        );

        var coursePlan = coursePlanRepository.findById(courseId)
                .orElseThrow(() -> ex);

        Course course = courseRepository.findByStudentAndCoursePlan(currentUser, coursePlan)
                .orElseThrow(() -> ex);

        course.getCompendiums().sort(Comparator.comparing(Compendium::getIndex));

        return basicMapper.courseToDetailedDto(course);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    public CourseBasicDto beginCourse(Long courseId, UserProfile currentUser) {

        var coursePlan = coursePlanRepository.findById(courseId)
             .orElseThrow(() -> new MyBadRequestException(
                     "CoursePlan with id '" + courseId + "' is no exists!"
             ));

        courseRepository.findByStudentAndCoursePlan(currentUser, coursePlan)
            .ifPresent(course -> {
                throw new MyBadRequestException(
                    "For the current user this course is already began!"
                );
            });

        Course course = new Course();
        course.setStudent(currentUser);
        course.setCoursePlan(coursePlan);
        course.setStartedAt(Instant.now());
        for (int i = 0; i < coursePlan.getTopics().size(); i++) {
            Compendium compendium = new Compendium();
            compendium.setCourse(course);
            compendium.setNotes("");
            compendium.setTopic(coursePlan.getTopics().get(i));
            compendium.setIndex(i);
            compendium.setStatus(i == 0? CompendiumStatus.CAN_START : CompendiumStatus.LOCKED);
            course.getCompendiums().add(compendium);
        }

        courseRepository.save(course);

        return basicMapper.courseToBasicDto(course, compendiumRepository, conceptRepository);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    public TopicCompendiumDto beginTopic(Long topicId, UserProfile currentUser, Long courseId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new MyBadRequestException(
                        "CoursePlan with id '" + topicId + "' is no exists!"
                ));

        Compendium compendium = compendiumRepository.findByTopicAndCourse_Student(topic, currentUser)
                .orElseThrow(() -> new MyBadRequestException(
                        "There is no compendium for user '"
                                + currentUser.getUsername()
                                + "' and topic '" + topic.getName()
                                + "'!"
                ));

        Long currentCompendiumId = compendium.getCourse().getCurrentCompendiumId();
        Optional<Compendium> currentCompendium = currentCompendiumId != null ?
                compendiumRepository.findById(currentCompendiumId) :
                Optional.empty();

        if (currentCompendium.isPresent()) {
            if (compendium.getIndex() != currentCompendium.get().getIndex() + 1)
                throw new MyBadRequestException("Wrong position of the compendium!");
        }
        else {
            if (compendium.getIndex() != 0)
                throw new MyBadRequestException("This topic is not first in the course plan!");
        }
        if (compendium.getIndex() < 0)
            throw new MyBadRequestException("Wrong position of the compendium! < 0");

        compendium.setStatus(CompendiumStatus.CURRENT);
        compendiumRepository.save(compendium);

        currentCompendium.ifPresent(c -> {
            c.setStatus(CompendiumStatus.COMPLETED);
            c.getConcepts().forEach(concept -> {
                if (concept.getIsFlashCard()) {
                    FlashCard flashCard = new FlashCard();
                    flashCard.setConcept(concept);
                    concept.setFlashCard(flashCard);
                }
            });
            compendiumRepository.save(c);
        });

        compendiumRepository
                .findByIndexAndTopic(compendium.getIndex() + 1, topic)
                .ifPresent(next -> {
                    next.setStatus(CompendiumStatus.CAN_START);
                    compendiumRepository.save(next);
                });

        compendium.getCourse().setCurrentCompendiumId(compendium.getId());
        courseRepository.save(compendium.getCourse());

        return basicMapper.compendiumToDto(compendium);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    public TopicCompendiumDto updateCompendium(TopicCompendiumDto topicCompendiumDto,
                                               UserProfile currentUser,
                                               Long courseId) {
        Compendium compendium = compendiumRepository.findById(topicCompendiumDto.id())
                .orElseThrow(() -> new MyBadRequestException(
                        "Compendium with id '" + topicCompendiumDto.id() + "' is no exists!"
                ));

        if (compendium.getStatus() != CompendiumStatus.CURRENT)
            throw new MyBadRequestException(
                "You can't modify this compendium!"
        );

        if (topicCompendiumDto.notes() != null) compendium.setNotes(topicCompendiumDto.notes());
        if (topicCompendiumDto.concepts() != null) {
            topicCompendiumDto.concepts().forEach(conceptDto -> {

                Optional<Concept> optionalConcept = (conceptDto.id() != null ?
                        conceptRepository.findById(conceptDto.id()) : Optional.empty());

                Concept concept = optionalConcept.orElseGet(() -> {
                    Concept c = new Concept();
                    c.setCompendium(compendium);
                    return c;
                });

                concept.setName(conceptDto.name());
                concept.setDescription(conceptDto.description());
                concept.setIsFlashCard(conceptDto.isFlashCard());

                if (optionalConcept.isEmpty())
                    compendium.getConcepts().add(concept);
            });

            compendium.getConcepts()
                    .removeIf(c -> topicCompendiumDto.concepts().stream()
                            .noneMatch(dto ->
                                    dto.id() == null || Long.valueOf(c.getId()).equals(dto.id()))
                    );
        }

        compendiumRepository.save(compendium);
        return basicMapper.compendiumToDto(compendium);
    }

    public TopicCompendiumDto beginTopic(Long topicId, UserProfile currentUser) {
        return beginTopic(topicId, currentUser, 1488L);
    }

    public TopicCompendiumDto updateCompendium(TopicCompendiumDto topicCompendiumDto, UserProfile currentUser) {
        return updateCompendium(topicCompendiumDto, currentUser, 1488L);
    }
}
