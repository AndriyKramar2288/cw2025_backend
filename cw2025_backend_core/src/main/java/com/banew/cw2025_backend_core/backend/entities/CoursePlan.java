package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CoursePlan {
    @Id
    @GeneratedValue
    private long id;
    @Column(length = 255, nullable = false)
    private String name;
    @Column(length = 4096)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserProfile author;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "coursePlan", orphanRemoval = true)
    @OrderColumn(name = "position")
    private List<Topic> topics = new ArrayList<>();
    @OneToMany(mappedBy = "coursePlan")
    private Set<Course> courses = new LinkedHashSet<>();
    private String backgroundSrc;
    @Formula("(select count(*) from course c where c.course_plan_id = id)")
    private long studentCount;
}