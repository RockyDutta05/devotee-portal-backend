package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    private final AdminAuditService adminAuditService;

    public List<UserDto> getPendingSignups(String search, String sortBy) {
        Sort sort = Sort.unsorted();
        if ("createdAt desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("createdAt asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        }
        List<User> users;
        if (search == null || search.trim().isEmpty()) {
            users = userRepository.findByApprovalStatus(ApprovalStatus.PENDING, sort);
        } else {
            users = userRepository.findPendingSignupsWithFilters(ApprovalStatus.PENDING, search, sort);
        }
        
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto approveUser(UUID id, String adminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        User savedUser = userRepository.save(user);
        notificationService.notifyAccountApproved(savedUser);
        adminAuditService.logAction(adminEmail, ActionType.SIGNUP_APPROVED, savedUser.getId(), "Approved signup");
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto rejectUser(UUID id, String adminEmail, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        User savedUser = userRepository.save(user);
        notificationService.notifyAccountRejected(savedUser);
        String details = reason != null && !reason.trim().isEmpty() ? "Rejected signup. Reason: " + reason : "Rejected signup";
        adminAuditService.logAction(adminEmail, ActionType.SIGNUP_REJECTED, savedUser.getId(), details);
        return mapToDto(savedUser);
    }

    @Transactional
    public void bulkApproveSignups(List<UUID> userIds, String adminEmail) {
        for (UUID id : userIds) {
            approveUser(id, adminEmail);
        }
    }

    @Transactional
    public void bulkRejectSignups(List<UUID> userIds, String adminEmail, String reason) {
        for (UUID id : userIds) {
            rejectUser(id, adminEmail, reason);
        }
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
