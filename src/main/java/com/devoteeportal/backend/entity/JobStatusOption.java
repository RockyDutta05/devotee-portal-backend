package com.devoteeportal.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "job_status_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String label;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
    }
}
