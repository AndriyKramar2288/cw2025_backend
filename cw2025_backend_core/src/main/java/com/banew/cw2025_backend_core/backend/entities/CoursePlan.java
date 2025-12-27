package com.banew.cw2025_backend_core.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlan {
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    @NotBlank
    private String name;
    @Column(length = 4096)
    @Nullable
    private String description;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserProfile author;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "coursePlan", orphanRemoval = true)
    @OrderColumn(name = "position")
    @NotNull
    @Builder.Default
    private List<Topic> topics = new ArrayList<>();
    @Builder.Default
    @NotNull
    @OneToMany(mappedBy = "coursePlan")
    private Set<Course> courses = new LinkedHashSet<>();
    private String backgroundSrc;
    @Builder.Default
    @NotNull
    private Boolean isPublic = true;
    @Formula("(select count(*) from course c where c.course_plan_id = id)")
    private long studentCount;
}