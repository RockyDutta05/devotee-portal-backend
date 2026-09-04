package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.AuthResponse;
import com.devoteeportal.backend.dto.LoginRequest;
import com.devoteeportal.backend.dto.SignupRequest;
import com.devoteeportal.backend.dto.UserDto;
import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.exception.PendingApprovalException;
import com.devoteeportal.backend.exception.RejectedApprovalException;
import com.devoteeportal.backend.repository.UserRepository;
import com.devoteeportal.backend.security.CustomUserDetailsService;
import com.devoteeportal.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final NotificationService notificationService;

    @Transactional
    public UserDto signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .initiatedName(request.getInitiatedName())
                .chantingRounds(request.getChantingRounds())
                .connectedToName(request.getConnectedToName())
                .connectedToContact(request.getConnectedToContact())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .currentEmployer(request.getCurrentEmployer())
                .hideEmployer(request.getHideEmployer())
                .build();

        User savedUser = userRepository.save(user);
        notificationService.notifyRegistrationReceived(savedUser);
        
        return mapToDto(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new PendingApprovalException("Your account is pending approval.");
        }
        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new RejectedApprovalException("Your account has been rejected.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getRole().name(), user.getApprovalStatus().name());

        return AuthResponse.builder()
                .token(token)
                .user(mapToDto(user))
                .build();
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .connectedToContact(user.getConnectedToContact())
                .email(user.getEmail())
                .phone(user.getPhone())
                .currentEmployer(user.getCurrentEmployer())
                .hideEmployer(user.getHideEmployer())
                .role(user.getRole())
                .approvalStatus(user.getApprovalStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
