package com.devoteeportal.backend.dto;

import lombok.Data;

@Data
public class PresignedUrlRequest {
    private String fileName;
    private String fileType;
}
