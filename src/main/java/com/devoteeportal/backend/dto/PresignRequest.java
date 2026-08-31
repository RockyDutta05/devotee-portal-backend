package com.devoteeportal.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignRequest {
    @NotNull
    private String fileName;
    
    @NotNull
    private String fileType;

    @NotNull
    @Max(value = 10485760, message = "File size cannot exceed 10 MB")
    private Long contentLength;
}
