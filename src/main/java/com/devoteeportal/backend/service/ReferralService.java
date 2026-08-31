package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.CompanyDto;
import com.devoteeportal.backend.dto.CreateReferralRequest;
import com.devoteeportal.backend.dto.ReferralRequestDto;
import com.devoteeportal.backend.dto.WillingReferrerDto;
import com.devoteeportal.backend.entity.Company;
import com.devoteeportal.backend.entity.ReferralCompany;
import com.devoteeportal.backend.entity.ReferralRequest;
import com.devoteeportal.backend.entity.ReferralRequestStatus;
import com.devoteeportal.backend.entity.ReferralWillingness;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.exception.ReferralLimitExceededException;
import com.devoteeportal.backend.repository.CompanyRepository;
import com.devoteeportal.backend.repository.ReferralCompanyRepository;
import com.devoteeportal.backend.repository.ReferralRequestRepository;
import com.devoteeportal.backend.repository.ReferralWillingnessRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralWillingnessRepository referralWillingnessRepository;
    private final ReferralCompanyRepository referralCompanyRepository;
    private final ReferralRequestRepository referralRequestRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AdminSettingsService adminSettingsService;
    private final NotificationService notificationService;

    public List<WillingReferrerDto> getWillingReferrers(UUID companyId) {
        List<WillingReferrerDto> referrers = new ArrayList<>();
        
        // 1. Direct company referrers first
        List<ReferralCompany> directCompanies = referralCompanyRepository.findByCompanyId(companyId);
        Set<UUID> directUserIds = directCompanies.stream()
                .map(rc -> rc.getUser().getId())
                .collect(Collectors.toSet());

        for (ReferralCompany rc : directCompanies) {
            User u = rc.getUser();
            referrers.add(WillingReferrerDto.builder()
                    .userId(u.getId())
                    .name(u.getName())
                    .initiatedName(u.getInitiatedName())
                    .currentEmployer(u.getCurrentEmployer()) // conditional rules can be applied if needed
                    .directCompanyMatch(true)
                    .build());
        }

        // 2. General willing referrers second
        List<ReferralWillingness> willingUsers = referralWillingnessRepository.findByIsWillingTrue();
        for (ReferralWillingness rw : willingUsers) {
            User u = rw.getUser();
            if (!directUserIds.contains(u.getId())) {
                referrers.add(WillingReferrerDto.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .initiatedName(u.getInitiatedName())
                        .currentEmployer(u.getCurrentEmployer())
                        .directCompanyMatch(false)
                        .build());
            }
        }

        return referrers;
    }

    @Transactional
    public ReferralRequestDto createReferralRequest(String email, CreateReferralRequest request) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        // Enforce Cap
        long pendingRequests = referralRequestRepository.countByRequesterIdAndStatus(requester.getId(), ReferralRequestStatus.PENDING);
        
        int cap = adminSettingsService.getReferralRequestCapPerPerson();
        if (pendingRequests >= cap) {
            throw new ReferralLimitExceededException("You have reached the maximum allowed pending referral requests (" + cap + ").");
        }

        User referrer = userRepository.findById(request.getReferrerId())
                .orElseThrow(() -> new RuntimeException("Referrer not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        ReferralRequest referralRequest = ReferralRequest.builder()
                .requester(requester)
                .referrer(referrer)
                .company(company)
                .jobIdOrLink(request.getJobIdOrLink())
                .jobTitle(request.getJobTitle())
                .comments(request.getComments())
                .status(ReferralRequestStatus.PENDING)
                .build();

        ReferralRequest savedRequest = referralRequestRepository.save(referralRequest);
        
        notificationService.notifyReferralRequestReceived(referrer, requester, company.getName());

        return ReferralRequestDto.builder()
                .id(savedRequest.getId())
                .requesterId(savedRequest.getRequester().getId())
                .referrerId(savedRequest.getReferrer().getId())
                .company(CompanyDto.builder()
                        .id(company.getId())
                        .name(company.getName())
                        .approved(company.getApproved())
                        .build())
                .jobIdOrLink(savedRequest.getJobIdOrLink())
                .jobTitle(savedRequest.getJobTitle())
                .comments(savedRequest.getComments())
                .status(savedRequest.getStatus())
                .createdAt(savedRequest.getCreatedAt())
                .updatedAt(savedRequest.getUpdatedAt())
                .build();
    }
    @Transactional
    public void setWillingness(String email, boolean isWilling) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ReferralWillingness willingness = referralWillingnessRepository.findByUserId(user.getId())
                .orElse(ReferralWillingness.builder().user(user).build());
        willingness.setIsWilling(isWilling);
        referralWillingnessRepository.save(willingness);
    }

    @Transactional
    public void addReferralCompany(String email, UUID companyId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        boolean exists = referralCompanyRepository.findByCompanyId(companyId).stream()
                .anyMatch(rc -> rc.getUser().getId().equals(user.getId()));
        
        if (!exists) {
            ReferralCompany rc = ReferralCompany.builder()
                    .user(user)
                    .company(company)
                    .build();
            referralCompanyRepository.save(rc);
        }
    }
}
