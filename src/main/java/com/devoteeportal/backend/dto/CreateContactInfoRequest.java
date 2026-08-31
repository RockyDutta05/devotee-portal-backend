package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContactInfoRequest {
    @NotNull
    private UUID targetId;
    
    @NotBlank(message = "Reason is mandatory")
    private String reason;
}
