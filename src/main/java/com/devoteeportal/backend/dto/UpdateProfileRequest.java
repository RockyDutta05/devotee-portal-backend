package com.devoteeportal.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String initiatedName;
    private Integer chantingRounds;
    private String connectedToName;
    private String connectedToContact;
    @jakarta.validation.constraints.Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;
    private String currentEmployer;
    private String jobTitle;
    private String location;
    private Boolean hideEmployer;
}
