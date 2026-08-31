package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class JobStatusOptionDto {
    private UUID id;
    private String label;
}
