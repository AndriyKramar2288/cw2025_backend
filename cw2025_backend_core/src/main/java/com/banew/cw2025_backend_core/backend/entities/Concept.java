package com.banew.cw2025_backend_core.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String name;
    @Column(length = 4096)
    @Nullable
    private String description;
    @Column(nullable = false)
    @NotNull
    private Boolean isFlashCard = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compendium_id", nullable = false)
    @NotNull
    private Compendium compendium;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "flashCard_id")
    @Nullable
    private FlashCard flashCard;
}