package com.devoteeportal.backend.config;

import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder, com.devoteeportal.backend.repository.JobStatusOptionRepository jobStatusOptionRepository) {
        return args -> {
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .name("System Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .approvalStatus(ApprovalStatus.APPROVED)
                        .build();
                userRepository.save(admin);
            }

            if (jobStatusOptionRepository.count() == 0) {
                java.util.List<String> statuses = java.util.Arrays.asList("Urgently Hiring", "Hiring", "Position Filled");
                for (String status : statuses) {
                    jobStatusOptionRepository.save(com.devoteeportal.backend.entity.JobStatusOption.builder()
                            .label(status)
                            .active(true)
                            .build());
                }
            }
        };
    }
}
