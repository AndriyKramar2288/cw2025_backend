package com.banew.cw2025_backend_common.dto.cards;

import com.banew.cw2025_backend_common.dto.courses.TopicCompendiumDto;

import java.util.Map;

public record FlashCardBasicDto(
    Long id,
    Map<FlashCardAnswer, Double> availableIntervals,
    TopicCompendiumDto.ConceptBasicDto concept
) { }