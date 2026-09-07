package com.devoteeportal.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service.account.path:}")
    private String firebaseServiceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (firebaseServiceAccountPath != null && !firebaseServiceAccountPath.isEmpty()) {
                    FileInputStream serviceAccount = new FileInputStream(firebaseServiceAccountPath);
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    log.info("Initializing FirebaseApp using credentials at: {}", firebaseServiceAccountPath);
                    return FirebaseApp.initializeApp(options);
                } else {
                    log.warn("FIREBASE_SERVICE_ACCOUNT_PATH is not set. FirebaseApp will not be fully initialized.");
                }
            } else {
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Failed to initialize FirebaseApp with path: {}", firebaseServiceAccountPath, e);
        }
        return null;
    }
}
