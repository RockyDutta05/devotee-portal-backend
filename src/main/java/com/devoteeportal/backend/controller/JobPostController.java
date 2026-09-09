package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.JobPostDto;
import com.devoteeportal.backend.dto.JobPostRequest;
import com.devoteeportal.backend.dto.JobStatusOptionDto;
import com.devoteeportal.backend.service.JobPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<JobPostDto>> getAllJobPosts(
            @org.springframework.data.web.PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(jobPostService.getAllJobPosts(pageable));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<JobStatusOptionDto>> getAllJobStatuses() {
        return ResponseEntity.ok(jobPostService.getAllJobStatuses());
    }

    @PostMapping
    public ResponseEntity<JobPostDto> createJobPost(Authentication authentication, @jakarta.validation.Valid @RequestBody JobPostRequest request) {
        return ResponseEntity.ok(jobPostService.createJobPost(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPostDto> updateJobPost(Authentication authentication, @PathVariable UUID id, @jakarta.validation.Valid @RequestBody JobPostRequest request) {
        return ResponseEntity.ok(jobPostService.updateJobPost(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobPost(Authentication authentication, @PathVariable UUID id) {
        jobPostService.deleteJobPost(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
