package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
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
    private String name;
    @Column(length = 4096)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserProfile author;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "coursePlan", orphanRemoval = true)
    @OrderColumn(name = "position")
    @Builder.Default
    private List<Topic> topics = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "coursePlan")
    private Set<Course> courses = new LinkedHashSet<>();
    private String backgroundSrc;
    @Builder.Default
    private Boolean isPublic = true;
    @Formula("(select count(*) from course c where c.course_plan_id = id)")
    private long studentCount;
}