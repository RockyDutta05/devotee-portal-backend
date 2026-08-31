package com.devoteeportal.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateReferralRequest {
    private UUID referrerId;
    private UUID companyId;
    private String jobIdOrLink;
    private String jobTitle;
    private String comments;
}
