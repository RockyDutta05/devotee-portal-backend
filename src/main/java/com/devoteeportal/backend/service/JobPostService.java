package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.CompanyDto;
import com.devoteeportal.backend.dto.JobPostDto;
import com.devoteeportal.backend.dto.JobPostRequest;
import com.devoteeportal.backend.dto.JobStatusOptionDto;
import com.devoteeportal.backend.entity.Company;
import com.devoteeportal.backend.entity.JobPost;
import com.devoteeportal.backend.entity.JobStatusOption;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.CompanyRepository;
import com.devoteeportal.backend.repository.JobPostRepository;
import com.devoteeportal.backend.repository.JobStatusOptionRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobStatusOptionRepository jobStatusOptionRepository;

    public List<JobPostDto> getAllJobPosts() {
        return jobPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<JobStatusOptionDto> getAllJobStatuses() {
        return jobStatusOptionRepository.findAll()
                .stream()
                .map(status -> JobStatusOptionDto.builder()
                        .id(status.getId())
                        .label(status.getLabel())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public JobPostDto createJobPost(String email, JobPostRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        JobStatusOption status = jobStatusOptionRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        JobPost jobPost = JobPost.builder()
                .postedBy(user)
                .title(request.getTitle())
                .company(company)
                .jobIdOrLink(request.getJobIdOrLink())
                .comments(request.getComments())
                .noticePeriodRequirement(request.getNoticePeriodRequirement())
                .status(status)
                .build();

        return mapToDto(jobPostRepository.save(jobPost));
    }

    @Transactional
    public JobPostDto updateJobPost(String email, UUID jobId, JobPostRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job Post not found"));

        if (!jobPost.getPostedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to update this job post");
        }

        if (request.getTitle() != null) jobPost.setTitle(request.getTitle());
        if (request.getJobIdOrLink() != null) jobPost.setJobIdOrLink(request.getJobIdOrLink());
        if (request.getComments() != null) jobPost.setComments(request.getComments());
        if (request.getNoticePeriodRequirement() != null) jobPost.setNoticePeriodRequirement(request.getNoticePeriodRequirement());

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            jobPost.setCompany(company);
        }

        if (request.getStatusId() != null) {
            JobStatusOption status = jobStatusOptionRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Status not found"));
            jobPost.setStatus(status);
        }

        return mapToDto(jobPostRepository.save(jobPost));
    }

    @Transactional
    public void deleteJobPost(String email, UUID jobId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job Post not found"));

        if (!jobPost.getPostedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this job post");
        }

        jobPostRepository.delete(jobPost);
    }

    private JobPostDto mapToDto(JobPost jobPost) {
        return JobPostDto.builder()
                .id(jobPost.getId())
                .postedBy(jobPost.getPostedBy().getId())
                .title(jobPost.getTitle())
                .company(CompanyDto.builder()
                        .id(jobPost.getCompany().getId())
                        .name(jobPost.getCompany().getName())
                        .approved(jobPost.getCompany().getApproved())
                        .build())
                .jobIdOrLink(jobPost.getJobIdOrLink())
                .comments(jobPost.getComments())
                .noticePeriodRequirement(jobPost.getNoticePeriodRequirement())
                .status(JobStatusOptionDto.builder()
                        .id(jobPost.getStatus().getId())
                        .label(jobPost.getStatus().getLabel())
                        .build())
                .createdAt(jobPost.getCreatedAt())
                .updatedAt(jobPost.getUpdatedAt())
                .build();
    }
}
