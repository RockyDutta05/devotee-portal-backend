package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.Resume;
import com.devoteeportal.backend.entity.ResumeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findByUserId(UUID userId);
    long countByUserId(UUID userId);
    Page<Resume> findByHiddenFromPublicSearchFalseAndStatus(ResumeStatus status, Pageable pageable);
}
