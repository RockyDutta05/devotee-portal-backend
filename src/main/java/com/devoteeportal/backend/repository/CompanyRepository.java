package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
