package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class JobPostRequest {
    @NotBlank(message = "Title is mandatory")
    private String title;
    
    @NotNull(message = "Company ID is mandatory")
    private UUID companyId;
    
    @NotBlank(message = "Job ID or Link is mandatory")
    private String jobIdOrLink;
    
    private String comments;
    
    private String noticePeriodRequirement;
    
    @NotNull(message = "Status is mandatory")
    private UUID statusId;
}
