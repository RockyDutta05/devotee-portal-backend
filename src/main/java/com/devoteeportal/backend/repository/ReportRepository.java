package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import com.devoteeportal.backend.entity.ReportStatus;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Query("SELECT r FROM Report r JOIN r.jobPost jp LEFT JOIN jp.company c WHERE (:#{#status == null} = true OR r.status = :status) AND (:#{#search == null} = true OR (LOWER(jp.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))))")
    List<Report> findReportsWithFilters(@org.springframework.data.repository.query.Param("status") ReportStatus status,
                                        @org.springframework.data.repository.query.Param("search") String search);
}
