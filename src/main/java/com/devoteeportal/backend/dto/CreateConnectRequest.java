package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConnectRequest {
    @NotNull
    private UUID targetId;
    
    @Size(max = 600, message = "Message maximum length is 600 characters")
    private String message;
}
