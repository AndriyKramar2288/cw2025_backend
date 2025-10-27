package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserProfile student;
    @ManyToOne
    @JoinColumn(name = "course_plan_id")
    private CoursePlan coursePlan;
    private Instant startedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", orphanRemoval = true)
    private List<Compendium> compendiums = new ArrayList<>();
    private Long currentCompendiumId;
}