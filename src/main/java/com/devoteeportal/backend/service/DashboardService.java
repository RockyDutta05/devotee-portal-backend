package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.DashboardStatsDto;
import com.devoteeportal.backend.entity.ReferralRequestStatus;
import com.devoteeportal.backend.entity.RequestStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobPostRepository jobPostRepository;
    private final ResumeRepository resumeRepository;
    private final ReferralWillingnessRepository referralWillingnessRepository;
    private final ReferralRequestRepository referralRequestRepository;
    private final ContactInfoRequestRepository contactInfoRequestRepository;
    private final ConnectRequestRepository connectRequestRepository;
    private final UserRepository userRepository;

    public DashboardStatsDto getDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        long availableJobs = jobPostRepository.countByStatusLabel("OPEN");
        long myResumes = resumeRepository.countByUserId(currentUser.getId());
        long referralOpportunities = referralWillingnessRepository.countByIsWillingTrue();
        
        long pendingReferrals = referralRequestRepository.countByReferrerIdAndStatus(currentUser.getId(), ReferralRequestStatus.PENDING);
        long pendingContacts = contactInfoRequestRepository.countByTargetIdAndStatus(currentUser.getId(), RequestStatus.PENDING) +
                               connectRequestRepository.countByTargetIdAndStatus(currentUser.getId(), RequestStatus.PENDING);

        return DashboardStatsDto.builder()
                .availableJobs(availableJobs)
                .myResumes(myResumes)
                .referralOpportunities(referralOpportunities)
                .pendingReferrals(pendingReferrals)
                .pendingContacts(pendingContacts)
                .build();
    }
}
