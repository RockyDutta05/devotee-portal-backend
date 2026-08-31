package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.ResumeStatus;
import lombok.Data;

@Data
public class ResumeRequest {
    private String title;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private ResumeStatus status;
    private String noticePeriod;
    private Boolean hiddenFromPublicSearch;
}
