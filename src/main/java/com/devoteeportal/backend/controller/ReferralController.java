package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.CreateReferralRequest;
import com.devoteeportal.backend.dto.ReferralRequestDto;
import com.devoteeportal.backend.dto.WillingReferrerDto;
import com.devoteeportal.backend.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/willing-referrers")
    public ResponseEntity<List<WillingReferrerDto>> getWillingReferrers(@RequestParam UUID companyId) {
        return ResponseEntity.ok(referralService.getWillingReferrers(companyId));
    }

    @PostMapping("/requests")
    public ResponseEntity<ReferralRequestDto> createReferralRequest(Authentication authentication, @RequestBody CreateReferralRequest request) {
        return ResponseEntity.ok(referralService.createReferralRequest(authentication.getName(), request));
    }

    @PostMapping("/willingness")
    public ResponseEntity<Void> setWillingness(Authentication authentication, @RequestBody java.util.Map<String, Boolean> request) {
        Boolean isWilling = request.get("isWilling");
        referralService.setWillingness(authentication.getName(), isWilling != null ? isWilling : false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/companies")
    public ResponseEntity<Void> addReferralCompany(Authentication authentication, @RequestBody java.util.Map<String, UUID> request) {
        UUID companyId = request.get("companyId");
        referralService.addReferralCompany(authentication.getName(), companyId);
        return ResponseEntity.ok().build();
    }
}
