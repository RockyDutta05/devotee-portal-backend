package com.devoteeportal.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String initiatedName;
    @jakarta.validation.constraints.Min(value = 0, message = "Chanting rounds cannot be less than 0")
    @jakarta.validation.constraints.Max(value = 128, message = "Chanting rounds cannot exceed 128")
    private Integer chantingRounds;
    private String connectedToName;
    private String connectedToDesignation;
    private String connectedToContact;
    @jakarta.validation.constraints.Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;
    private String currentEmployer;
    private String jobTitle;
    private String location;
    private Boolean hideEmployer;
    private String photoUrl;
}
