package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConnectRequestDto {
    private UUID id;
    private UUID requesterId;
    private String requesterName;
    private UUID targetId;
    private String message;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
