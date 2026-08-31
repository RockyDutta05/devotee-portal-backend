package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CompanyDto {
    private UUID id;
    private String name;
    private Boolean approved;
}
