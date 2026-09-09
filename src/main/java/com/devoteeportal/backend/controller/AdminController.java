package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.AdminDashboardStatsDto;
import com.devoteeportal.backend.dto.AdminActionLogDto;
import com.devoteeportal.backend.dto.BulkActionRequest;
import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.service.AdminAuditService;
import com.devoteeportal.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminAuditService adminAuditService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/pending")
    public ResponseEntity<List<UserDto>> getPendingSignups(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(adminService.getPendingSignups(search, sortBy));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/companies/pending")
    public ResponseEntity<List<Object>> getPendingCompanies() {
        // Placeholder: return empty list for now
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<UserDto> approveUser(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(adminService.approveUser(id, authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<UserDto> rejectUser(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(adminService.rejectUser(id, authentication.getName(), null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bulk-approve")
    public ResponseEntity<Void> bulkApproveUsers(@RequestBody BulkActionRequest request, Authentication authentication) {
        adminService.bulkApproveSignups(request.getUserIds(), authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bulk-reject")
    public ResponseEntity<Void> bulkRejectUsers(@RequestBody BulkActionRequest request, Authentication authentication) {
        adminService.bulkRejectSignups(request.getUserIds(), authentication.getName(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit-log")
    public ResponseEntity<Page<AdminActionLogDto>> getAuditLogs(
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(adminAuditService.getAuditLogs(actionType, startDate, endDate, pageable));
    }

}
