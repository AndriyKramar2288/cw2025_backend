package com.banew.cw2025_backend_core.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CoursePlan {
    @Id
    private long id;
    @Column(length = 255, nullable = false)
    private String name;
}
