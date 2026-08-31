package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
}
