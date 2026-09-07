package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ReferralWillingness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralWillingnessRepository extends JpaRepository<ReferralWillingness, UUID> {
    List<ReferralWillingness> findByIsWillingTrue();
    long countByIsWillingTrue();
    java.util.Optional<ReferralWillingness> findByUserId(UUID userId);
}
