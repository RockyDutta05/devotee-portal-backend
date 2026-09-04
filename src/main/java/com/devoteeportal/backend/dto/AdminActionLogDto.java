package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ActionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminActionLogDto {
    private UUID id;
    private String adminUserEmail;
    private ActionType actionType;
    private UUID targetId;
    private String details;
    private LocalDateTime createdAt;
}
