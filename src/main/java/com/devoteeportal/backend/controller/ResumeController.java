package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.PresignRequest;
import com.devoteeportal.backend.dto.PresignResponse;
import com.devoteeportal.backend.dto.ResumeRequest;
import com.devoteeportal.backend.dto.ResumeResponse;
import com.devoteeportal.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/mine")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getMyResumes(authentication.getName()));
    }

    @GetMapping("/browse")
    public ResponseEntity<org.springframework.data.domain.Page<ResumeResponse>> browsePublicResumes(
            @org.springframework.data.web.PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(resumeService.browsePublicResumes(pageable));
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(Authentication authentication, @RequestBody ResumeRequest request) {
        return ResponseEntity.ok(resumeService.createResume(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> updateResume(Authentication authentication, @PathVariable UUID id, @RequestBody ResumeRequest request) {
        return ResponseEntity.ok(resumeService.updateResume(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(Authentication authentication, @PathVariable UUID id) {
        resumeService.deleteResume(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/presign-upload")
    public ResponseEntity<PresignResponse> generatePresignedUrl(Authentication authentication, @jakarta.validation.Valid @RequestBody PresignRequest request) {
        return ResponseEntity.ok(resumeService.generatePresignedUrl(authentication.getName(), request));
    }
}
