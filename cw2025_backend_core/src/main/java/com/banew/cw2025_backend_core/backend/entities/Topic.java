package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Topic {
    @Id
    private long id;
    @Column(length = 255, nullable = false)
    private String name;
    @Lob
    private String description;
    @ManyToOne
    @JoinColumn(name = "course_plan_id", nullable = false)
    private CoursePlan coursePlan;
}