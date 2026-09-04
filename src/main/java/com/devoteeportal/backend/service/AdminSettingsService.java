package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.AdminSettingsDto;
import com.devoteeportal.backend.dto.UpdateAdminSettingsRequest;
import com.devoteeportal.backend.entity.ActionType;
import com.devoteeportal.backend.entity.AdminSettings;
import com.devoteeportal.backend.repository.AdminSettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private final AdminSettingsRepository adminSettingsRepository;
    private final AdminAuditService adminAuditService;

    @PostConstruct
    public void initSettings() {
        if (adminSettingsRepository.count() == 0) {
            AdminSettings settings = new AdminSettings();
            settings.setReferralRequestCapPerPerson(3);
            adminSettingsRepository.save(settings);
        }
    }

    public AdminSettingsDto getSettings() {
        AdminSettings settings = adminSettingsRepository.findAll().get(0);
        return AdminSettingsDto.builder()
                .referralRequestCapPerPerson(settings.getReferralRequestCapPerPerson())
                .build();
    }

    public int getReferralRequestCapPerPerson() {
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
