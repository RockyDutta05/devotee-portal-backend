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
            // Ensure admin user with correct credentials exists
            java.util.Optional<com.devoteeportal.backend.entity.User> existing = userRepository.findByEmail("admin@example.com");
            if (existing.isPresent()) {
                com.devoteeportal.backend.entity.User admin = existing.get();
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setApprovalStatus(ApprovalStatus.APPROVED);
                userRepository.save(admin);
            } else if (!userRepository.existsByEmail("admin@gmail.com")) {
                com.devoteeportal.backend.entity.User admin = com.devoteeportal.backend.entity.User.builder()
                        .name("System Admin")
                        .email("admin@gmail.com")
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
