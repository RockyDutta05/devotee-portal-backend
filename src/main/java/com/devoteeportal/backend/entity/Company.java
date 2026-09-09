package com.devoteeportal.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", nullable = true)
    private User addedByUserId;

    @Column(nullable = false)
    private Boolean approved;

    @PrePersist
    public void prePersist() {
        if (this.approved == null) {
            this.approved = false;
        }
    }
}
