package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    @Lob
    private String description;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserProfile author;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "coursePlan", orphanRemoval = true)
    @OrderColumn(name = "position")
    private List<Topic> topics;
    @OneToMany(mappedBy = "coursePlan")
    private List<Course> courses;
}