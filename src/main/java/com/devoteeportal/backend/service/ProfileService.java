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
        if (request.getConnectedToContact() != null) user.setConnectedToContact(request.getConnectedToContact());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCurrentEmployer() != null) user.setCurrentEmployer(request.getCurrentEmployer());
        if (request.getHideEmployer() != null) user.setHideEmployer(request.getHideEmployer());

        User savedUser = userRepository.save(user);
        return mapToMyProfile(savedUser);
    }

    public PublicProfileResponse getPublicProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return PublicProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .currentEmployer(Boolean.TRUE.equals(user.getHideEmployer()) ? null : user.getCurrentEmployer())
                .build();
    }

    private MyProfileResponse mapToMyProfile(User user) {
        return MyProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .connectedToContact(user.getConnectedToContact())
                .email(user.getEmail())
                .phone(user.getPhone())
                .currentEmployer(user.getCurrentEmployer())
                .hideEmployer(user.getHideEmployer())
                .role(user.getRole())
                .approvalStatus(user.getApprovalStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
