package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.AdminActionLogDto;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.AdminActionLog;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.AdminActionLogRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logAction(String adminEmail, ActionType actionType, UUID targetId, String details) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        AdminActionLog log = AdminActionLog.builder()
                .adminUser(admin)
                .actionType(actionType)
                .targetId(targetId)
                .details(details)
                .build();
                
        adminActionLogRepository.save(log);
    }

    public Page<AdminActionLogDto> getAuditLogs(ActionType actionType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return adminActionLogRepository.findAuditLogsWithFilters(actionType, startDate, endDate, pageable)
                .map(log -> AdminActionLogDto.builder()
                        .id(log.getId())
                        .adminUserEmail(log.getAdminUser().getEmail())
                        .actionType(log.getActionType())
                        .targetId(log.getTargetId())
                        .details(log.getDetails())
                        .createdAt(log.getCreatedAt())
                        .build());
    }
}
