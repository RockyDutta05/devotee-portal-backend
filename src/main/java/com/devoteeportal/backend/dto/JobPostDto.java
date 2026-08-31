package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JobPostDto {
    private UUID id;
    private UUID postedBy;
    private String title;
    private CompanyDto company;
    private String jobIdOrLink;
    private String comments;
    private String noticePeriodRequirement;
    private JobStatusOptionDto status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
