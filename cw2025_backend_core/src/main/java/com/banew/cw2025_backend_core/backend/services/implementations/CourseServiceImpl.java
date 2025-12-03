package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import com.banew.cw2025_backend_common.dto.courses.CourseBasicDto;
import com.banew.cw2025_backend_common.dto.courses.CourseDetailedDto;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.*;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.CompendiumRepository;
import com.banew.cw2025_backend_core.backend.repo.ConceptRepository;
import com.banew.cw2025_backend_core.backend.repo.CoursePlanRepository;
import com.banew.cw2025_backend_core.backend.repo.CourseRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.CourseService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private CoursePlanRepository coursePlanRepository;
    private CourseRepository courseRepository;
    private ConceptRepository conceptRepository;
    private CompendiumRepository compendiumRepository;
    private BasicMapper basicMapper;
    private CacheManager cacheManager;

    @Override
    @Cacheable(value = "courses", key = "#currentUser.id")
    public List<CourseBasicDto> getUserCourses(UserProfile currentUser) {
        return courseRepository.findUserCourses(currentUser).stream()
                .map(basicMapper::courseDbDataToBasicDto)
                .toList();
    }

    @Override
    @Cacheable(value = "courseById", key = "#courseId + '_' + #currentUser.id")
    public CourseDetailedDto getCourseById(UserProfile currentUser, Long courseId) {
        return basicMapper.courseToDetailedDto(
                courseRepository.findByStudentAndCoursePlanIdWithFetch(currentUser.getId(), courseId)
                        .orElseThrow(() -> new MyBadRequestException(
                        "CoursePlan with id '" + courseId + "' and this user is no exists!"))
        );
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    @Transactional
    public CourseBasicDto beginCourse(Long courseId, UserProfile currentUser) {

        if (courseRepository.existsByStudentAndCoursePlanId(currentUser, courseId))
            throw new MyBadRequestException(
                    "For the current user this course is already began!"
            );

        var coursePlan = coursePlanRepository
                .findByIdWithTopics(courseId)
                .filter(cp -> cp.getIsPublic() || cp.getAuthor().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new MyBadRequestException(
                        "CoursePlan with id '" + courseId + "' is no exists!"
                ));

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

        return courseToBasicDto(course);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    @Transactional
    public CourseDetailedDto endCourse(Long courseId, UserProfile currentUser) {

        Course course = courseRepository.findByStudentAndCoursePlanId(currentUser, courseId)
                .orElseThrow(() -> new MyBadRequestException(
                        "There is no compendium for user '"
                                + currentUser.getUsername()
                                + "' and this course-plan!'"
                                + "'!"
                ));

        var currentCompendium = course.getCurrentCompendium();
        if (currentCompendium == null)
            throw new MyBadRequestException(
                    "This course isn't started or already ended!"
            );

        course.setCurrentCompendium(null);
        endTopic(currentCompendium);
        courseRepository.save(course);

        return basicMapper.courseToDetailedDto(course);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id")
            }
    )
    @Transactional
    public TopicCompendiumDto beginTopic(Long topicId, UserProfile currentUser, Long courseId) {

        Compendium compendium = compendiumRepository.findByTopicIdAndStudentWithCourse(topicId, currentUser)
                .orElseThrow(() -> new MyBadRequestException(
                        "There is no compendium for user '"
                                + currentUser.getUsername()
                                + "' and this topic!"
                ));

        if (compendium.getStatus() != CompendiumStatus.LOCKED && compendium.getStatus() != CompendiumStatus.CAN_START)
            throw new MyBadRequestException("You can't begin this topic!");

        Optional<Compendium> currentCompendium = Optional.ofNullable(compendium.getCourse().getCurrentCompendium());

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

        currentCompendium.ifPresent(this::endTopic);

        compendiumRepository
                .findByTopicIdAndStudentAndIndex(topicId, currentUser, compendium.getIndex() + 1)
                .ifPresent(next -> {
                    next.setStatus(CompendiumStatus.CAN_START);
                    compendiumRepository.save(next);
                });

        compendium.getCourse().setCurrentCompendium(compendium);
        courseRepository.save(compendium.getCourse());

        return basicMapper.compendiumToDto(compendium);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "courses", key = "#currentUser.id"),
                    @CacheEvict(value = "courseById", key = "#courseId + '_' + #currentUser.id"),
                    @CacheEvict(value = "flashCardStatsByUserId", key = "#currentUser.id"),
                    @CacheEvict(value = "flashCardsByUserId", key = "#currentUser.id")
            }
    )
    @Transactional
    public TopicCompendiumDto updateCompendium(TopicCompendiumDto topicCompendiumDto,
                                               UserProfile currentUser,
                                               Long courseId) {
        Compendium compendium = compendiumRepository.findByIdWithConcepts(topicCompendiumDto.id())
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
                        compendium.getConcepts().stream()
                                .filter(c -> conceptDto.id().equals(c.getId()))
                                .findFirst() : Optional.empty());

                Concept concept = optionalConcept.orElseGet(() -> {
                    Concept c = new Concept();
                    c.setCompendium(compendium);
                    compendium.getConcepts().add(c);
                    return c;
                });

                concept.setName(conceptDto.name());
                concept.setDescription(conceptDto.description());
                concept.setIsFlashCard(conceptDto.isFlashCard());
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

    @Override
    @CacheEvict(value = "courses", key = "#authorId")
    public void evictByAuthorId(Long authorId) {
        coursePlanRepository.findIdByAuthorId(authorId).forEach(e -> {
            Cache cache = cacheManager.getCache("courseById");
            if (cache != null) cache.evict(e + "_" + authorId);
        });
    }

    private CourseBasicDto courseToBasicDto(Course course) {
        Optional<Compendium> compendium =
                Optional.ofNullable(course.getCurrentCompendium());

        String topicName = compendium.stream()
                .map(c -> c.getTopic().getName())
                .findFirst().orElse(null);

        return new CourseBasicDto(
                course.getId(), course.getStartedAt(), basicMapper.coursePlanToCourseDto(course.getCoursePlan()),
                topicName,
                conceptRepository.countByCompendium_Course(course),
                compendiumRepository.countByCourse(course)
        );
    }

    @Transactional
    private void endTopic(Compendium c) {
        c.setStatus(CompendiumStatus.COMPLETED);
        c.getConcepts().forEach(concept -> {
            if (concept.getIsFlashCard()) {
                FlashCard flashCard = new FlashCard();
                flashCard.setConcept(concept);
                concept.setFlashCard(flashCard);
            }
        });
        compendiumRepository.save(c);
    }
}
