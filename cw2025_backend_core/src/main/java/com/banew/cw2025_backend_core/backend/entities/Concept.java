package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Concept {
    @Id
    @GeneratedValue
    private long id;
    @Column(length = 255, nullable = false)
    private String name;
    @Column(length = 4096)
    private String description;
    @Column(nullable = false)
    private Boolean isFlashCard = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compendium_id", nullable = false)
    private Compendium compendium;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "flashCard_id")
    private FlashCard flashCard;
}