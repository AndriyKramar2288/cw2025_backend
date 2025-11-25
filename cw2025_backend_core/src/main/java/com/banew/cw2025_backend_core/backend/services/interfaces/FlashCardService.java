package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_common.dto.cards.FlashCardAnswer;
import com.banew.cw2025_backend_common.dto.cards.FlashCardBasicDto;
import com.banew.cw2025_backend_common.dto.cards.FlashCardDayStats;
import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;

import java.util.List;

public interface FlashCardService {
    List<FlashCardBasicDto> getCards(UserProfile currentUser);
    FlashCardBasicDto updateConcept(UserProfile currentUser,
                                    Long flashCardId,
                                    TopicCompendiumDto.ConceptBasicDto newConcept);
    FlashCardBasicDto answer(FlashCardAnswer answer, UserProfile currentUser, Long flashCardId);
    FlashCardDayStats getDayStats(UserProfile currentUser);
}
