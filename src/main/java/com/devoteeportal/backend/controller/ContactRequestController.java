package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.ContactInfoRequestDto;
import com.devoteeportal.backend.dto.CreateContactInfoRequest;
import com.devoteeportal.backend.entity.RequestStatus;
import com.devoteeportal.backend.service.NetworkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contact-requests")
@RequiredArgsConstructor
public class ContactRequestController {

    private final NetworkingService networkingService;

    @PostMapping
    public ResponseEntity<ContactInfoRequestDto> createRequest(Authentication authentication, @jakarta.validation.Valid @RequestBody CreateContactInfoRequest request) {
        return ResponseEntity.ok(networkingService.createContactInfoRequest(authentication.getName(), request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ContactInfoRequestDto> approveRequest(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(networkingService.updateContactInfoRequestStatus(authentication.getName(), id, RequestStatus.APPROVED));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ContactInfoRequestDto> rejectRequest(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(networkingService.updateContactInfoRequestStatus(authentication.getName(), id, RequestStatus.REJECTED));
    }

    @GetMapping("/incoming")
    public ResponseEntity<java.util.List<ContactInfoRequestDto>> getIncomingRequests(Authentication authentication) {
        return ResponseEntity.ok(networkingService.getIncomingContactRequests(authentication.getName()));
    }
}
