package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, UUID> {

    @Query("SELECT log FROM AdminActionLog log WHERE " +
           "(:actionType IS NULL OR log.actionType = :actionType) AND " +
           "(:startDate IS NULL OR log.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR log.createdAt <= :endDate)")
    Page<AdminActionLog> findAuditLogsWithFilters(
            @Param("actionType") ActionType actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
