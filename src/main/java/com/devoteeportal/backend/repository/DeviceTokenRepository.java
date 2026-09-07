package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.DeviceToken;
import com.devoteeportal.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    List<DeviceToken> findByUserId(UUID userId);
    Optional<DeviceToken> findByUserAndFcmToken(User user, String fcmToken);
}
