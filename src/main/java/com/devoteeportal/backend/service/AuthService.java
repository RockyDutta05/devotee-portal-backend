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

import com.devoteeportal.backend.dto.PresignRequest;
import com.devoteeportal.backend.dto.PresignResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final NotificationService notificationService;
    private final S3Presigner s3Presigner;
    private final com.devoteeportal.backend.repository.OtpRepository otpRepository;

    @Value("${app.r2.bucket-name}")
    private String bucketName;
    
    @Value("${app.r2.endpoint-url}")
    private String endpointUrl;

    @Transactional
    public UserDto signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        com.devoteeportal.backend.entity.Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email has not been verified"));

        if (!otp.isVerified()) {
            throw new IllegalArgumentException("Email has not been verified");
        }

        User user = User.builder()
                .name(request.getName())
                .initiatedName(request.getInitiatedName())
                .chantingRounds(request.getChantingRounds())
                .connectedToName(request.getConnectedToName())
                .connectedToDesignation(request.getConnectedToDesignation())
                .connectedToContact(request.getConnectedToContact())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .currentEmployer(request.getCurrentEmployer())
                .hideEmployer(request.getHideEmployer())
                .photoUrl(request.getPhotoUrl())
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

    public PresignResponse generateProfilePicturePresignedUrl(PresignRequest request) {
        String fileName = request.getFileName();
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }
        java.util.List<String> allowedExtensions = java.util.List.of("jpg", "jpeg", "png");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions are: " + String.join(", ", allowedExtensions));
        }

        if (request.getContentLength() != null && request.getContentLength() > 5242880) {
            throw new IllegalArgumentException("File size cannot exceed 5 MB for profile pictures");
        }

        String objectKey = "profiles/temp/" + UUID.randomUUID() + "-" + request.getFileName();
        String publicFileUrl = endpointUrl + "/" + bucketName + "/" + objectKey;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getFileType())
                .contentLength(request.getContentLength())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build());

        return PresignResponse.builder()
                .presignedUrl(presignedRequest.url().toString())
                .fileUrl(publicFileUrl)
                .objectKey(objectKey)
                .build();
    }

    @Transactional
    public void generateAndSendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Generate 4-digit code
        String code = String.format("%04d", new java.util.Random().nextInt(10000));
        
        com.devoteeportal.backend.entity.Otp otp = com.devoteeportal.backend.entity.Otp.builder()
                .email(email)
                .code(code)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
                
        // Delete any old OTPs for this email to prevent clutter
        otpRepository.deleteByEmail(email);
        otpRepository.save(otp);
        
        notificationService.sendOtpEmail(email, code);
    }

    @Transactional
    public void verifyOtp(String email, String code) {
        com.devoteeportal.backend.entity.Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found for this email"));

        if (otp.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!otp.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }

    private UserDto mapToDto(User user) {
        String finalFileUrl = user.getPhotoUrl();
        String prefix = endpointUrl + "/" + bucketName + "/";
        if (finalFileUrl != null && finalFileUrl.startsWith(prefix)) {
            try {
                String objectKey = finalFileUrl.substring(prefix.length());
                software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest)
                        .build());
                finalFileUrl = presignedRequest.url().toString();
            } catch (Exception e) {
                // Fallback to original URL if presigning fails
            }
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .initiatedName(user.getInitiatedName())
                .chantingRounds(user.getChantingRounds())
                .connectedToName(user.getConnectedToName())
                .connectedToDesignation(user.getConnectedToDesignation())
                .connectedToContact(user.getConnectedToContact())
                .email(user.getEmail())
                .phone(user.getPhone())
                .currentEmployer(user.getCurrentEmployer())
                .hideEmployer(user.getHideEmployer())
                .photoUrl(finalFileUrl)
                .role(user.getRole())
                .approvalStatus(user.getApprovalStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
