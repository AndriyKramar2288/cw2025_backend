package com.banew.cw2025_backend_core.backend.entities;

import com.banew.cw2025_backend_common.dto.courses.CompendiumStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Compendium {
    @Id
    @GeneratedValue
    private long id;
    @Column(length = 4096)
    private String notes;
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    public CompendiumStatus status;
    private int index;
    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true, mappedBy = "compendium")
    @OrderBy("id ASC")
    private List<Concept> concepts = new ArrayList<>();
}
