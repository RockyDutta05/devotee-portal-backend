package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    private String name;

    private String initiatedName;

    @jakarta.validation.constraints.Min(value = 0, message = "Chanting rounds cannot be less than 0")
    @jakarta.validation.constraints.Max(value = 128, message = "Chanting rounds cannot exceed 128")
    private Integer chantingRounds;

    private String connectedToName;
    private String connectedToDesignation;
    private String connectedToContact;

    

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    private String role;

    private String currentEmployer;
    private Boolean hideEmployer;
    private String photoUrl;

}
