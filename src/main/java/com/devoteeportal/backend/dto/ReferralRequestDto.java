package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ReferralRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReferralRequestDto {
    private UUID id;
    private UUID requesterId;
    private UUID referrerId;
    private CompanyDto company;
    private String jobIdOrLink;
    private String jobTitle;
    private String comments;
    private ReferralRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
