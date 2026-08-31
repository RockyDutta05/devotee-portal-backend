package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    private String name;

    private String initiatedName;

    private Integer chantingRounds;

    private String connectedToName;
    private String connectedToContact;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String phone;
    private String currentEmployer;
    private Boolean hideEmployer;
}
