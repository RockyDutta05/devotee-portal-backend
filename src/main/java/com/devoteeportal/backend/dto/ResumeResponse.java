package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ResumeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResumeResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private ResumeStatus status;
    private String noticePeriod;
    private Boolean hiddenFromPublicSearch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
