package com.devoteeportal.backend.service;

import com.devoteeportal.backend.entity.Resume;
import com.devoteeportal.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2CleanupScheduler {

    private final ResumeRepository resumeRepository;
    private final S3Client s3Client;

    @Value("${app.r2.bucket-name}")
    private String bucketName;

    @Value("${app.r2.endpoint-url}")
    private String endpointUrl;

    // Run every 12 hours
    @Scheduled(fixedRate = 43200000)
    public void cleanupOrphanedResumes() {
        log.info("Starting R2 cleanup job...");
        try {
            List<Resume> allResumes = resumeRepository.findAll();
            Set<String> dbObjectKeys = allResumes.stream()
                    .map(Resume::getFileUrl)
                    .filter(url -> url != null && url.startsWith(endpointUrl + "/" + bucketName + "/"))
                    .map(url -> url.substring((endpointUrl + "/" + bucketName + "/").length()))
                    .collect(Collectors.toSet());

            boolean isTruncated = true;
            String continuationToken = null;
            Set<String> r2ObjectKeys = new java.util.HashSet<>();
            Set<S3Object> allS3Objects = new java.util.HashSet<>();

            while (isTruncated) {
                ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix("resumes/")
                        .continuationToken(continuationToken)
                        .build();

                ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
                for(S3Object obj : listResponse.contents()) {
                    r2ObjectKeys.add(obj.key());
                    allS3Objects.add(obj);
                }
                
                isTruncated = listResponse.isTruncated() != null && listResponse.isTruncated();
                continuationToken = listResponse.nextContinuationToken();
            }

            // 1. Find orphaned DB rows (in DB but not in R2)
            int dbOrphansDeleted = 0;
            for (Resume resume : allResumes) {
                String fileUrl = resume.getFileUrl();
                if (fileUrl != null && fileUrl.startsWith(endpointUrl + "/" + bucketName + "/")) {
                    String objectKey = fileUrl.substring((endpointUrl + "/" + bucketName + "/").length());
                    if (!r2ObjectKeys.contains(objectKey)) {
                        log.info("Deleting orphaned DB row for resume ID: {}", resume.getId());
                        resumeRepository.delete(resume);
                        dbOrphansDeleted++;
                    }
                }
            }

            // 2. Find abandoned R2 uploads (in R2 but not in DB, older than 24h)
            int r2OrphansDeleted = 0;
            Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
            for (S3Object s3Object : allS3Objects) {
                if (!dbObjectKeys.contains(s3Object.key())) {
                    if (s3Object.lastModified().isBefore(twentyFourHoursAgo)) {
                        log.info("Deleting abandoned R2 object: {}", s3Object.key());
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(s3Object.key())
                                .build());
                        r2OrphansDeleted++;
                    }
                }
            }

            log.info("R2 cleanup job completed. Deleted {} orphaned DB rows and {} abandoned R2 objects.", dbOrphansDeleted, r2OrphansDeleted);
        } catch (Exception e) {
            log.error("Error during R2 cleanup job", e);
        }
    }
}
