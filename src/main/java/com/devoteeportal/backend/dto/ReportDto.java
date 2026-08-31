package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReportDto {
    private UUID id;
    private UUID jobPostId;
    private UUID reporterId;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
