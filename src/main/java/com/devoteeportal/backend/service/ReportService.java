package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.CreateReportRequest;
import com.devoteeportal.backend.dto.ReportDto;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.JobPost;
import com.devoteeportal.backend.entity.Report;
import com.devoteeportal.backend.entity.ReportStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.JobPostRepository;
import com.devoteeportal.backend.repository.ReportRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final AdminAuditService adminAuditService;

    @Transactional
    public ReportDto createReport(String email, CreateReportRequest request) {
        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobPost jobPost = jobPostRepository.findById(request.getJobPostId())
                .orElseThrow(() -> new RuntimeException("Job Post not found"));

        Report report = Report.builder()
                .jobPost(jobPost)
                .reporter(reporter)
                .reason(request.getReason())
                .status(ReportStatus.PENDING)
                .build();

        return mapToDto(reportRepository.save(report));
    }

    public List<ReportDto> getAllReports(ReportStatus status, String search) {
        return reportRepository.findReportsWithFilters(status, search)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportDto reviewReport(UUID reportId, String adminEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(ReportStatus.REVIEWED);
        Report savedReport = reportRepository.save(report);
        
        adminAuditService.logAction(adminEmail, ActionType.REPORT_REVIEWED, savedReport.getId(), "Reviewed report for job post: " + savedReport.getJobPost().getTitle());
        
        return mapToDto(savedReport);
    }

    private ReportDto mapToDto(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .jobPostId(report.getJobPost().getId())
                .reporterId(report.getReporter().getId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
