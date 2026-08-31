package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/signups")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/pending")
    public ResponseEntity<List<UserDto>> getPendingSignups() {
        return ResponseEntity.ok(adminService.getPendingSignups());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<UserDto> approveUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.approveUser(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<UserDto> rejectUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.rejectUser(id));
    }
}
