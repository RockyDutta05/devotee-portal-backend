package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, UUID> {
    Page<JobPost> findAll(Pageable pageable);
    long countByStatusLabelIgnoreCase(String label);
}
