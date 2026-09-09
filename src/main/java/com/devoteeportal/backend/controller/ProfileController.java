package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.MyProfileResponse;
import com.devoteeportal.backend.dto.PublicProfileResponse;
import com.devoteeportal.backend.dto.UpdateProfileRequest;
import com.devoteeportal.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<MyProfileResponse> updateMyProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMyProfile(authentication.getName(), request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(Authentication authentication, @PathVariable UUID userId) {
        String requesterEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(profileService.getPublicProfile(requesterEmail, userId));
    }
}
