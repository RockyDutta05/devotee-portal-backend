package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.DeviceTokenRequest;
import com.devoteeportal.backend.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/register-device")
    public ResponseEntity<Void> registerDeviceToken(Authentication authentication, @Valid @RequestBody DeviceTokenRequest request) {
        pushNotificationService.registerDeviceToken(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
