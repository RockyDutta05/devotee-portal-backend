package com.devoteeportal.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String initiatedName;
    private Integer chantingRounds;
    private String connectedToName;
    private String connectedToContact;
    private String phone;
    private String currentEmployer;
    private Boolean hideEmployer;
}
