package com.devoteeportal.backend;

import com.devoteeportal.backend.dto.CreateReferralRequest;
import com.devoteeportal.backend.entity.*;
import com.devoteeportal.backend.exception.ReferralLimitExceededException;
import com.devoteeportal.backend.repository.CompanyRepository;
import com.devoteeportal.backend.repository.UserRepository;
import com.devoteeportal.backend.service.ReferralService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ReferralCapIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReferralService referralService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void testReferralCapEnforcementPerReferrer() {
        User requester = User.builder().name("Requester").email("requester@example.com").password("pass").role(Role.USER).approvalStatus(ApprovalStatus.APPROVED).build();
        User referrerA = User.builder().name("Referrer A").email("referrera@example.com").password("pass").role(Role.USER).approvalStatus(ApprovalStatus.APPROVED).build();
        User referrerB = User.builder().name("Referrer B").email("referrerb@example.com").password("pass").role(Role.USER).approvalStatus(ApprovalStatus.APPROVED).build();

        User savedRequester = userRepository.save(requester);
        User savedReferrerA = userRepository.save(referrerA);
        User savedReferrerB = userRepository.save(referrerB);

        Company company = companyRepository.save(Company.builder().name("Test Corp").approved(true).build());

        // Create 3 requests to Referrer A (Cap is 3 by default)
        for (int i = 0; i < 3; i++) {
            CreateReferralRequest req = new CreateReferralRequest();
            req.setReferrerId(savedReferrerA.getId());
            req.setCompanyId(company.getId());
            req.setJobIdOrLink("LINK" + i);
            req.setJobTitle("Job " + i);
            referralService.createReferralRequest(savedRequester.getEmail(), req);
        }

        // 4th request to Referrer A should fail
        CreateReferralRequest req4 = new CreateReferralRequest();
        req4.setReferrerId(savedReferrerA.getId());
        req4.setCompanyId(company.getId());
        req4.setJobIdOrLink("LINK4");
        req4.setJobTitle("Job 4");
        
        assertThrows(ReferralLimitExceededException.class, () -> {
            referralService.createReferralRequest(savedRequester.getEmail(), req4);
        });

        // 1st request to Referrer B should succeed!
        CreateReferralRequest reqB = new CreateReferralRequest();
        reqB.setReferrerId(savedReferrerB.getId());
        reqB.setCompanyId(company.getId());
        reqB.setJobIdOrLink("LINK_B");
        reqB.setJobTitle("Job B");
        
        assertDoesNotThrow(() -> {
            referralService.createReferralRequest(savedRequester.getEmail(), reqB);
        });
    }
}
