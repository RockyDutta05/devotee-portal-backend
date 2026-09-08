package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
