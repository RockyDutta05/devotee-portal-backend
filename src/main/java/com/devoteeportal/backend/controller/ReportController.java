package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.CreateReportRequest;
import com.devoteeportal.backend.dto.ReportDto;
import com.devoteeportal.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportDto> createReport(Authentication authentication, @jakarta.validation.Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReportDto>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/review")
    public ResponseEntity<ReportDto> reviewReport(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.reviewReport(id));
    }
}
