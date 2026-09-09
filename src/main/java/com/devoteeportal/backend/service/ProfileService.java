package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.MyProfileResponse;
import com.devoteeportal.backend.dto.PublicProfileResponse;
import com.devoteeportal.backend.dto.UpdateProfileRequest;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final com.devoteeportal.backend.repository.ContactInfoRequestRepository contactRequestRepository;

    public MyProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToMyProfile(user);
    }

    @Transactional
    public MyProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (request.getName() != null) user.setName(request.getName());
        if (request.getInitiatedName() != null) user.setInitiatedName(request.getInitiatedName());
        if (request.getChantingRounds() != null) user.setChantingRounds(request.getChantingRounds());
        if (request.getConnectedToName() != null) user.setConnectedToName(request.getConnectedToName());
        if (request.getConnectedToDesignation() != null) user.setConnectedToDesignation(request.getConnectedToDesignation());
        if (request.getConnectedToContact() != null) user.setConnectedToContact(request.getConnectedToContact());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCurrentEmployer() != null) user.setCurrentEmployer(request.getCurrentEmployer());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getHideEmployer() != null) user.setHideEmployer(request.getHideEmployer());
        if (request.getPhotoUrl() != null) user.setPhotoUrl(request.getPhotoUrl());

        User savedUser = userRepository.save(user);
        return mapToMyProfile(savedUser);
    }

    public PublicProfileResponse getPublicProfile(String requesterEmail, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        boolean isApprovedContact = false;
        if (requesterEmail != null) {
            User requester = userRepository.findByEmail(requesterEmail).orElse(null);
            if (requester != null) {
                if (requester.getId().equals(userId)) {
                    isApprovedContact = true;
                } else {
                    isApprovedContact = contactRequestRepository.existsByRequesterIdAndTargetIdAndStatus(
                            requester.getId(), userId, com.devoteeportal.backend.entity.RequestStatus.APPROVED);
                }
            }
        }
        
        return PublicProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .connectedToDesignation(user.getConnectedToDesignation())
                .currentEmployer(Boolean.TRUE.equals(user.getHideEmployer()) ? null : user.getCurrentEmployer())
                .jobTitle(user.getJobTitle())
                .location(user.getLocation())
                .photoUrl(user.getPhotoUrl())
                .email(isApprovedContact ? user.getEmail() : null)
                .phone(isApprovedContact ? user.getPhone() : null)
                .build();
    }

    private MyProfileResponse mapToMyProfile(User user) {
        return MyProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .connectedToDesignation(user.getConnectedToDesignation())
                .connectedToContact(user.getConnectedToContact())
                .email(user.getEmail())
                .phone(user.getPhone())
                .currentEmployer(user.getCurrentEmployer())
                .jobTitle(user.getJobTitle())
                .location(user.getLocation())
                .hideEmployer(user.getHideEmployer())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole())
                .approvalStatus(user.getApprovalStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
