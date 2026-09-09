package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.Role;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserDto {
    private UUID id;
    private String name;
    private String initiatedName;
    private Integer chantingRounds;
    private String connectedToName;
    private String connectedToContact;
    private String connectedToDesignation;
    private String email;
    private String phone;
    private String currentEmployer;
    private String jobTitle;
    private String location;
    private String photoUrl;
    private Boolean hideEmployer;
    private Role role;
    @Getter(onMethod = @__(@JsonProperty("status")))
    @JsonProperty("status")
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
