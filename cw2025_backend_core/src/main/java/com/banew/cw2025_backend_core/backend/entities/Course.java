package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
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
    private UserProfile student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_plan_id")
    private CoursePlan coursePlan;
    private Instant startedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", orphanRemoval = true)
    @OrderBy("index ASC")
    private Set<Compendium> compendiums = new LinkedHashSet<>();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_compendium_id")
    private Compendium currentCompendium;
}