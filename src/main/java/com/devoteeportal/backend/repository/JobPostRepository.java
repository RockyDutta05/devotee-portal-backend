package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, UUID> {
    List<JobPost> findAllByOrderByCreatedAtDesc();
}
