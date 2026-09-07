package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ReferralRequest;
import com.devoteeportal.backend.entity.ReferralRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, UUID> {
    long countByRequesterIdAndStatus(UUID requesterId, ReferralRequestStatus status);
    long countByReferrerIdAndStatus(UUID referrerId, ReferralRequestStatus status);
    java.util.List<ReferralRequest> findByReferrerIdOrderByCreatedAtDesc(UUID referrerId);
}
