package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WillingReferrerDto {
    private UUID userId;
    private String name;
    private String initiatedName;
    private String currentEmployer;
    private boolean directCompanyMatch;
}
