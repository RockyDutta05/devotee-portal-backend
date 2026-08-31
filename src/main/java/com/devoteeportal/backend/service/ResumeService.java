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

    public List<ResumeResponse> browsePublicResumes() {
        return resumeRepository.findByHiddenFromPublicSearchFalseAndStatus(ResumeStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
                .status(request.getStatus() != null ? request.getStatus() : ResumeStatus.ACTIVE)
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

        String objectKey = "resumes/" + user.getId() + "/" + UUID.randomUUID() + "-" + request.getFileName();
        
        // Remove https:// or http:// if included in endpointUrl, or just use as base for fileUrl
        // A common pattern is to just format the public URL if R2 public bucket is used, or the R2 endpoint itself.
        String publicFileUrl = endpointUrl + "/" + bucketName + "/" + objectKey;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
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
        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUser().getId())
                .title(resume.getTitle())
                .fileUrl(resume.getFileUrl())
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
