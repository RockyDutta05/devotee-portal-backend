package com.devoteeportal.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignedUrlResponse {
    private String presignedUrl;
    private String fileUrl;
    private String fileKey;
}
