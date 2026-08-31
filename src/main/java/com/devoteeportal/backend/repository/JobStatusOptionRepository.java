package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.JobStatusOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobStatusOptionRepository extends JpaRepository<JobStatusOption, UUID> {
    Optional<JobStatusOption> findByLabel(String label);
    boolean existsByLabel(String label);
}
