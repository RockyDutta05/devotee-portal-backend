package com.devoteeportal.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "referral_willingness")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralWillingness {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Boolean isWilling;

    @PrePersist
    public void prePersist() {
        if (this.isWilling == null) {
            this.isWilling = false;
        }
    }
}
