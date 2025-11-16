package com.banew.cw2025_backend_core.backend.services.implementations;

import com.banew.cw2025_backend_common.dto.cards.FlashCardAnswer;
import com.banew.cw2025_backend_common.dto.cards.FlashCardBasicDto;
import com.banew.cw2025_backend_common.dto.cards.FlashCardDayStats;
import com.banew.cw2025_backend_common.dto.cards.FlashCardType;
import com.banew.cw2025_backend_core.backend.entities.FlashCard;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.FlashCardRepository;
import com.banew.cw2025_backend_core.backend.services.interfaces.FlashCardService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class FlashCardServiceImpl implements FlashCardService {

    private FlashCardRepository flashCardRepository;
    private BasicMapper basicMapper;

    @Override
    @Transactional
    public List<FlashCardBasicDto> getCards(UserProfile currentUser) {
        return flashCardRepository.availableCards(currentUser, Instant.now()).stream()
                .map(card -> fromFlashCard(card, null))
                .toList();
    }

    @Override
    @Transactional
    public FlashCardBasicDto answer(FlashCardAnswer answer, UserProfile currentUser, Long flashCardId) {

        FlashCard flashCard = flashCardRepository.findById(flashCardId)
                .orElseThrow(() -> new MyBadRequestException(
                        "Card with id '" + flashCardId + "' is no exists!"
                ));

        if (!flashCard.isShouldReview()) {
            throw new MyBadRequestException(
                    "You cannot review this card! The time has not yet come!"
            );
        }

        return fromFlashCard(flashCard, answer);
    }

    @Override
    @Transactional
    public FlashCardDayStats getDayStats(UserProfile currentUser) {

        List<FlashCard> todayCards = flashCardRepository
                .findByConcept_Compendium_Course_StudentAndLastReviewAfter(
                        currentUser, Instant.now().minus(1, ChronoUnit.DAYS)
                );

        List<Duration> durations = IntStream.range(0, todayCards.size() - 1)
                .mapToObj(i -> Duration.between(
                        todayCards.get(i).getLastReview(),
                        todayCards.get(i + 1).getLastReview()))
                .filter(i -> i.toSeconds() < 15)
                .toList();

        return new FlashCardDayStats(
                Map.of(
                        FlashCardType.NEW, (int) flashCardRepository.countNewCards(currentUser),
                        FlashCardType.REPEAT, (int) flashCardRepository.countReviewCards(currentUser, Instant.now()),
                        FlashCardType.STUDY, (int) flashCardRepository.countStudyCards(currentUser, Instant.now())
                ),
                todayCards.size(),
                !durations.isEmpty() ?
                        durations.stream().reduce(Duration.ZERO, Duration::plus).dividedBy(durations.size())
                        : Duration.ZERO
        );
    }

    private FlashCardBasicDto fromFlashCard(FlashCard flashCard, FlashCardAnswer optionalAnswer) {
        if (optionalAnswer != null) {
            resolveCard(flashCard, optionalAnswer).insertData(flashCard);
            flashCardRepository.save(flashCard);
        }

        return new FlashCardBasicDto(
                flashCard.getId(),
                Arrays.stream(FlashCardAnswer.values())
                        .collect(Collectors.toMap(
                                v -> v, answer -> resolveCard(flashCard, answer).interval()
                        )),
                basicMapper.conceptToDto(flashCard.getConcept())
        );
    }

    private static FlashCardResolveResult resolveCard(FlashCard flashCard, FlashCardAnswer flashCardAnswer) {
        return resolveCard(
                flashCard.getRepetition(), flashCard.getInterval(), flashCard.getEasiness(), flashCardAnswer.getQuality()
        );
    }

    private static FlashCardResolveResult resolveCard(int repetitions,
                                               double interval,
                                               double easiness,
                                               int quality) {
        if (quality < 3) {
            repetitions = 0;
            interval = 0;
        }
        else {
            if (repetitions == 0) interval = 1;
            else if (repetitions == 1) interval = 6;
            else interval = Math.round(interval * easiness);
            repetitions++;
        }

        easiness = easiness + (0.1 - (5 - quality)*(0.08 + (5-quality)*0.02));

        return new FlashCardResolveResult(
                repetitions, interval, easiness
        );
    }

    private record FlashCardResolveResult(
            int repetitions,
            double interval,
            double easiness
    ) {
        public void insertData(FlashCard flashCard) {
            flashCard.setInterval(interval);
            flashCard.setRepetition(repetitions);
            flashCard.setEasiness(easiness);
            flashCard.setLastReview(Instant.now());
        }
    }
}
