package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.AdminSettingsDto;
import com.devoteeportal.backend.dto.UpdateAdminSettingsRequest;
import com.devoteeportal.backend.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<AdminSettingsDto> getSettings() {
        return ResponseEntity.ok(adminSettingsService.getSettings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/referral-cap")
    public ResponseEntity<AdminSettingsDto> updateReferralCap(@RequestBody UpdateAdminSettingsRequest request, org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(adminSettingsService.updateReferralCap(request, authentication.getName()));
    }
}
