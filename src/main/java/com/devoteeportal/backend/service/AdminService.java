package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<UserDto> getPendingSignups() {
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto approveUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        User savedUser = userRepository.save(user);
        notificationService.notifyAccountApproved(savedUser);
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto rejectUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        User savedUser = userRepository.save(user);
        notificationService.notifyAccountRejected(savedUser);
        return mapToDto(savedUser);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
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
