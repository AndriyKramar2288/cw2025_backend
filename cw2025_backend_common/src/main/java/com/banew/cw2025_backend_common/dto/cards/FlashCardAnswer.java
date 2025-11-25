package com.banew.cw2025_backend_common.dto.cards;

public enum FlashCardAnswer {
    FAIL(1),
    BAD(3),
    GOOD(4),
    EASY(5);

    private final int quality;

    FlashCardAnswer(int quality) {
        this.quality = quality;
    }

    public int getQuality() {
        return quality;
    }
}
