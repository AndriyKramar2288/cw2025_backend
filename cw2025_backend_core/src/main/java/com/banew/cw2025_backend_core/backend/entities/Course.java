package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Course {
    @Id
    private long id;
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserProfile student;
}
