package com.banew.cw2025_backend_core.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Topic {
    @Id
    @GeneratedValue
    private long id;
    @Column(length = 255, nullable = false)
    @NotBlank
    private String name;
    @Column(length = 4096)
    @Nullable
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_plan_id", nullable = false)
    @NotNull
    private CoursePlan coursePlan;
}