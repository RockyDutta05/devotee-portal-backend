package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.AuthResponse;
import com.devoteeportal.backend.dto.LoginRequest;
import com.devoteeportal.backend.dto.SignupRequest;
import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignupRequest request) {
        UserDto userDto = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
