package com.banew.cw2025_backend_core.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull
    private UserProfile student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_plan_id", nullable = false)
    @NotNull
    private CoursePlan coursePlan;
    @NotNull
    private Instant startedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", orphanRemoval = true)
    @OrderBy("index ASC")
    @NotNull
    private Set<Compendium> compendiums = new LinkedHashSet<>();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_compendium_id")
    @Nullable
    private Compendium currentCompendium;
}