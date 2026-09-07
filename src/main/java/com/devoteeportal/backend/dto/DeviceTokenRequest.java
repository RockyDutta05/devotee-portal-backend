package com.devoteeportal.backend.dto;

import com.devoteeportal.backend.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    @NotNull(message = "Device platform is required")
    private DevicePlatform platform;
}
