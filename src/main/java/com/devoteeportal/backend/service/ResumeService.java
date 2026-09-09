package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.PresignRequest;
import com.devoteeportal.backend.dto.PresignResponse;
import com.devoteeportal.backend.dto.ResumeRequest;
import com.devoteeportal.backend.dto.ResumeResponse;
import com.devoteeportal.backend.entity.Resume;
import com.devoteeportal.backend.entity.ResumeStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.ResumeRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final S3Presigner s3Presigner;

    @Value("${app.r2.bucket-name}")
    private String bucketName;
    
    @Value("${app.r2.endpoint-url}")
    private String endpointUrl;

    public List<ResumeResponse> getMyResumes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return resumeRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<ResumeResponse> browsePublicResumes(org.springframework.data.domain.Pageable pageable) {
        return resumeRepository.findByHiddenFromPublicSearchFalseAndStatus(ResumeStatus.ACTIVELY_LOOKING, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public ResumeResponse createResume(String email, ResumeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resume resume = Resume.builder()
                .user(user)
                .title(request.getTitle())
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .status(request.getStatus() != null ? request.getStatus() : ResumeStatus.ACTIVELY_LOOKING)
                .noticePeriod(request.getNoticePeriod())
                .hiddenFromPublicSearch(request.getHiddenFromPublicSearch() != null ? request.getHiddenFromPublicSearch() : false)
                .build();

        return mapToDto(resumeRepository.save(resume));
    }

    @Transactional
    public ResumeResponse updateResume(String email, UUID resumeId, ResumeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to update this resume");
        }

        if (request.getTitle() != null) resume.setTitle(request.getTitle());
        if (request.getFileUrl() != null) resume.setFileUrl(request.getFileUrl());
        if (request.getFileName() != null) resume.setFileName(request.getFileName());
        if (request.getFileType() != null) resume.setFileType(request.getFileType());
        if (request.getStatus() != null) resume.setStatus(request.getStatus());
        if (request.getNoticePeriod() != null) resume.setNoticePeriod(request.getNoticePeriod());
        if (request.getHiddenFromPublicSearch() != null) resume.setHiddenFromPublicSearch(request.getHiddenFromPublicSearch());

        return mapToDto(resumeRepository.save(resume));
    }

    @Transactional
    public void deleteResume(String email, UUID resumeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this resume");
        }

        resumeRepository.delete(resume);
    }

    public PresignResponse generatePresignedUrl(String email, PresignRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate file extension
        String fileName = request.getFileName();
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }
        java.util.List<String> allowedExtensions = java.util.List.of("pdf", "doc", "docx", "jpg", "jpeg", "png");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions are: " + String.join(", ", allowedExtensions));
        }

        String objectKey = "resumes/" + user.getId() + "/" + UUID.randomUUID() + "-" + request.getFileName();
        
        // Remove https:// or http:// if included in endpointUrl, or just use as base for fileUrl
        // A common pattern is to just format the public URL if R2 public bucket is used, or the R2 endpoint itself.
        String publicFileUrl = endpointUrl + "/" + bucketName + "/" + objectKey;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getFileType())
                .contentLength(request.getContentLength())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build());

        return PresignResponse.builder()
                .presignedUrl(presignedRequest.url().toString())
                .fileUrl(publicFileUrl)
                .objectKey(objectKey)
                .build();
    }

    private ResumeResponse mapToDto(Resume resume) {
        String finalFileUrl = resume.getFileUrl();
        String prefix = endpointUrl + "/" + bucketName + "/";
        if (finalFileUrl != null && finalFileUrl.startsWith(prefix)) {
            try {
                String objectKey = finalFileUrl.substring(prefix.length());
                software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest)
                        .build());
                finalFileUrl = presignedRequest.url().toString();
            } catch (Exception e) {
                // Fallback to original URL if presigning fails
            }
        }

        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUser().getId())
                .userName(resume.getUser().getName())
                .userEmail(resume.getUser().getEmail())
                .userJobTitle(resume.getUser().getJobTitle())
                .userLocation(resume.getUser().getLocation())
                .title(resume.getTitle())
                .fileUrl(finalFileUrl)
                .fileName(resume.getFileName())
                .fileType(resume.getFileType())
                .status(resume.getStatus())
                .noticePeriod(resume.getNoticePeriod())
                .hiddenFromPublicSearch(resume.getHiddenFromPublicSearch())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
