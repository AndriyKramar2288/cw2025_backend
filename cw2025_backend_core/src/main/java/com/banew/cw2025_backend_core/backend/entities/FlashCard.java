package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class FlashCard {
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    private int repetition = 0;
    @Column(nullable = false)
    private double interval = 0;
    @Column(nullable = false)
    private double easiness = 2.5;
    private Instant lastReview;
    private Instant dueReview; // after this instant it should be reviewed
    @OneToOne(mappedBy = "flashCard")
    private Concept concept;

    public void setLastReview(Instant lastReview) {
        this.lastReview = lastReview;
        dueReview = Instant.now().plus((long) (interval * 24 * 60), ChronoUnit.MINUTES);
    }

    public Boolean isShouldReview() {
        if (lastReview == null) return true;

        return lastReview
                .plus((long) (interval * 24 * 60), ChronoUnit.MINUTES)
                .isBefore(Instant.now());
    }
}