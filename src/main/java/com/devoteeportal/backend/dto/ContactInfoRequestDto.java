package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactInfoRequestDto {
    private UUID id;
    private UUID requesterId;
    private UUID targetId;
    private String reason;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // These fields are only populated if status == APPROVED
    private String contactPhone;
    private String contactEmail;
}
