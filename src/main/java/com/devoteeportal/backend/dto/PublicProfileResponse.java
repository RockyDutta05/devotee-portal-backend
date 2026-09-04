package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PublicProfileResponse {
    private UUID id;
    private String name;
    private String initiatedName;
    private Integer chantingRounds;
    private String connectedToName;
    private String currentEmployer; 
    private String jobTitle;
    private String location;
}
