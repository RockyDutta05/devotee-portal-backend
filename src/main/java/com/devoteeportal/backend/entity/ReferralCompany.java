package com.devoteeportal.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "referral_companies", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "company_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
