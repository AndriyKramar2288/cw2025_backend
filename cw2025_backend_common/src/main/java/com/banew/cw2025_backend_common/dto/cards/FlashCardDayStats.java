package com.banew.cw2025_backend_common.dto.cards;

import java.time.Duration;
import java.util.Map;

public record FlashCardDayStats(
    Map<FlashCardType, Integer> cardLefts,
    Integer reviewNumber,
    Duration reviewDuration
) { }