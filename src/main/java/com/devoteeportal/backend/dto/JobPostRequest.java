package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class JobPostRequest {
    @NotBlank(message = "Title is mandatory")
    private String title;
    
    private UUID companyId;
    
    private String companyNameRaw;
    
    private String jobIdOrLink;
    
    private String comments;
    
    private String noticePeriodRequirement;
    
    @NotNull(message = "Status is mandatory")
    private UUID statusId;
}
