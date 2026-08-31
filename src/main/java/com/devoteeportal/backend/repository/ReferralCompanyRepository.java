package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ReferralCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralCompanyRepository extends JpaRepository<ReferralCompany, UUID> {
    List<ReferralCompany> findByCompanyId(UUID companyId);
}
