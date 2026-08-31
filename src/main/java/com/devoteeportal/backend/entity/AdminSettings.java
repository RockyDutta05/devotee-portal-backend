package com.devoteeportal.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer referralRequestCapPerPerson;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.referralRequestCapPerPerson == null) {
            this.referralRequestCapPerPerson = 3; // Default
        }
    }
}
