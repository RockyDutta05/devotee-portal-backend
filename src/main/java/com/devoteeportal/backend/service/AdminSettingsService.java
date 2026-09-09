package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.AdminSettingsDto;
import com.devoteeportal.backend.dto.UpdateAdminSettingsRequest;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.AdminSettings;
import com.devoteeportal.backend.repository.AdminSettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private final AdminSettingsRepository adminSettingsRepository;
    private final AdminAuditService adminAuditService;
    private final Environment env;

    @PostConstruct
    public void initSettings() {
        // Skip initialization in test profile to avoid H2 returning clause issues
        if (java.util.Arrays.asList(env.getActiveProfiles()).contains("test")) {
            return;
        }
        if (adminSettingsRepository.count() == 0) {
            AdminSettings settings = new AdminSettings();
            settings.setReferralRequestCapPerPerson(3);
            adminSettingsRepository.save(settings);
        }
    }

    public AdminSettingsDto getSettings() {
        if (adminSettingsRepository.count() == 0) {
            AdminSettings defaultSettings = new AdminSettings();
            defaultSettings.setReferralRequestCapPerPerson(3);
            return AdminSettingsDto.builder()
                .referralRequestCapPerPerson(defaultSettings.getReferralRequestCapPerPerson())
                .build();
        }
        AdminSettings settings = adminSettingsRepository.findAll().get(0);
        return AdminSettingsDto.builder()
                .referralRequestCapPerPerson(settings.getReferralRequestCapPerPerson())
                .build();
    }

    public int getReferralRequestCapPerPerson() {
        if (adminSettingsRepository.count() == 0) {
            return 3; // default cap for tests
        }
        return adminSettingsRepository.findAll().get(0).getReferralRequestCapPerPerson();
    }

    @Transactional
    public AdminSettingsDto updateReferralCap(UpdateAdminSettingsRequest request, String adminEmail) {
        AdminSettings settings = adminSettingsRepository.findAll().get(0);
        int oldVal = settings.getReferralRequestCapPerPerson();
        settings.setReferralRequestCapPerPerson(request.getReferralRequestCapPerPerson());
        AdminSettings updated = adminSettingsRepository.save(settings);
        
        adminAuditService.logAction(adminEmail, ActionType.REFERRAL_CAP_UPDATED, null, "Updated referral cap from " + oldVal + " to " + updated.getReferralRequestCapPerPerson());
        
        return AdminSettingsDto.builder()
                .referralRequestCapPerPerson(updated.getReferralRequestCapPerPerson())
                .build();
    }
}
