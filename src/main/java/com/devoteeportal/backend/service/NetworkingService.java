package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.ConnectRequestDto;
import com.devoteeportal.backend.dto.ContactInfoRequestDto;
import com.devoteeportal.backend.dto.CreateConnectRequest;
import com.devoteeportal.backend.dto.CreateContactInfoRequest;
import com.devoteeportal.backend.entity.ConnectRequest;
import com.devoteeportal.backend.entity.ContactInfoRequest;
import com.devoteeportal.backend.entity.RequestStatus;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.ConnectRequestRepository;
import com.devoteeportal.backend.repository.ContactInfoRequestRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NetworkingService {

    private final ContactInfoRequestRepository contactInfoRequestRepository;
    private final ConnectRequestRepository connectRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ContactInfoRequestDto createContactInfoRequest(String email, CreateContactInfoRequest request) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User target = userRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Target not found"));

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is mandatory");
        }

        ContactInfoRequest contactInfoRequest = ContactInfoRequest.builder()
                .requester(requester)
                .target(target)
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        ContactInfoRequest savedRequest = contactInfoRequestRepository.save(contactInfoRequest);
        notificationService.notifyContactRequestReceived(target, requester);
        return mapToDto(savedRequest);
    }

    @Transactional
    public ContactInfoRequestDto updateContactInfoRequestStatus(String targetEmail, UUID requestId, RequestStatus status) {
        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        ContactInfoRequest request = contactInfoRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTarget().getId().equals(targetUser.getId())) {
            throw new RuntimeException("Not authorized to update this request");
        }

        request.setStatus(status);
        ContactInfoRequest savedRequest = contactInfoRequestRepository.save(request);
        
        if (status == RequestStatus.APPROVED) {
            notificationService.notifyContactRequestApproved(request.getRequester(), targetUser);
        }
        
        return mapToDto(savedRequest);
    }

    @Transactional
    public ConnectRequestDto createConnectRequest(String email, CreateConnectRequest request) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User target = userRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Target not found"));

        if (request.getMessage() != null && request.getMessage().length() > 600) {
            throw new IllegalArgumentException("Message maximum length is 600 characters");
        }

        ConnectRequest connectRequest = ConnectRequest.builder()
                .requester(requester)
                .target(target)
                .message(request.getMessage())
                .status(RequestStatus.PENDING)
                .build();

        return mapToDto(connectRequestRepository.save(connectRequest));
    }

    @Transactional
    public ConnectRequestDto updateConnectRequestStatus(String targetEmail, UUID requestId, RequestStatus status) {
        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        ConnectRequest request = connectRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTarget().getId().equals(targetUser.getId())) {
            throw new RuntimeException("Not authorized to update this request");
        }

        request.setStatus(status);
        return mapToDto(connectRequestRepository.save(request));
    }

    private ContactInfoRequestDto mapToDto(ContactInfoRequest request) {
        ContactInfoRequestDto.ContactInfoRequestDtoBuilder builder = ContactInfoRequestDto.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getName())
                .targetId(request.getTarget().getId())
                .reason(request.getReason())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt());

        // Privacy Rule: Only expose email/phone if approved
        if (request.getStatus() == RequestStatus.APPROVED) {
            builder.contactEmail(request.getTarget().getEmail());
            builder.contactPhone(request.getTarget().getPhone());
        }

        return builder.build();
    }

    private ConnectRequestDto mapToDto(ConnectRequest request) {
        return ConnectRequestDto.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getName())
                .targetId(request.getTarget().getId())
                .message(request.getMessage())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<ContactInfoRequestDto> getIncomingContactRequests(String email) {
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return contactInfoRequestRepository.findByTargetId(targetUser.getId())
                .stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<ConnectRequestDto> getIncomingConnectRequests(String email) {
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return connectRequestRepository.findByTargetId(targetUser.getId())
                .stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
