package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.DeviceTokenRequest;
import com.devoteeportal.backend.entity.DeviceToken;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.DeviceTokenRepository;
import com.devoteeportal.backend.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerDeviceToken(String email, DeviceTokenRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserAndFcmToken(user, request.getFcmToken());
        if (existingToken.isEmpty()) {
            DeviceToken newToken = DeviceToken.builder()
                    .user(user)
                    .fcmToken(request.getFcmToken())
                    .platform(request.getPlatform())
                    .build();
            deviceTokenRepository.save(newToken);
            log.info("Registered new device token for user: {}", user.getId());
        } else {
            log.info("Device token already registered for user: {}", user.getId());
        }
    }

    public void sendToUser(UUID userId, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp is not initialized. Skipping push notification to user: {}", userId);
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("No device tokens found for user: {}. Skipping push notification.", userId);
            return;
        }

        for (DeviceToken token : tokens) {
            try {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(token.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());
                
                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
                log.info("Successfully sent message to token: {}. Response: {}", token.getFcmToken(), response);
            } catch (Exception e) {
                log.error("Failed to send message to token: {}", token.getFcmToken(), e);
            }
        }
    }
}
