package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.ConnectRequestDto;
import com.devoteeportal.backend.dto.CreateConnectRequest;
import com.devoteeportal.backend.entity.RequestStatus;
import com.devoteeportal.backend.service.NetworkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/connect-requests")
@RequiredArgsConstructor
public class ConnectRequestController {

    private final NetworkingService networkingService;

    @PostMapping
    public ResponseEntity<ConnectRequestDto> createRequest(Authentication authentication, @jakarta.validation.Valid @RequestBody CreateConnectRequest request) {
        return ResponseEntity.ok(networkingService.createConnectRequest(authentication.getName(), request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ConnectRequestDto> approveRequest(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(networkingService.updateConnectRequestStatus(authentication.getName(), id, RequestStatus.APPROVED));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ConnectRequestDto> rejectRequest(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(networkingService.updateConnectRequestStatus(authentication.getName(), id, RequestStatus.REJECTED));
    }
}
