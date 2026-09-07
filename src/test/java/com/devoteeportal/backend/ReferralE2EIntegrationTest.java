package com.devoteeportal.backend;

import com.devoteeportal.backend.dto.*;
import com.devoteeportal.backend.entity.*;
import com.devoteeportal.backend.repository.*;
import com.devoteeportal.backend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReferralE2EIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JobStatusOptionRepository jobStatusOptionRepository;
    @Autowired
    private JobPostService jobPostService;
    @Autowired
    private ReferralService referralService;
    @Autowired
    private NetworkingService networkingService;
    @Autowired
    private ReferralRequestRepository referralRequestRepository;
    @Autowired
    private ReferralWillingnessRepository referralWillingnessRepository;

    @Test
    public void testEndToEndReferralFlow() {
        // 1. Setup User A
        User userA = User.builder()
                .name("User A")
                .email("usera@example.com")
                .password("password")
                .role(Role.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        userA = userRepository.save(userA);

        // 2. Setup User B
        User userB = User.builder()
                .name("User B")
                .email("userb@example.com")
                .password("password")
                .role(Role.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        userB = userRepository.save(userB);

        // 3. User A toggles Willing to Refer
        ReferralWillingness willingness = ReferralWillingness.builder()
                .user(userA)
                .isWilling(true)
                .build();
        referralWillingnessRepository.save(willingness);

        // 4. Create Company & Job Status for User A's Job
        Company company = Company.builder()
                .name("Test Company")
                .approved(true)
                .build();
        company = companyRepository.save(company);

        JobStatusOption status = JobStatusOption.builder()
                .label("OPEN")
                .active(true)
                .build();
        status = jobStatusOptionRepository.save(status);

        // 5. User A posts a Job
        JobPostRequest jobReq = new JobPostRequest();
        jobReq.setTitle("Software Engineer");
        jobReq.setCompanyId(company.getId());
        jobReq.setJobIdOrLink("LINK123");
        jobReq.setStatusId(status.getId());
        jobReq.setNoticePeriodRequirement("30 Days");

        JobPostDto jobPost = jobPostService.createJobPost(userA.getEmail(), jobReq);
        assertNotNull(jobPost);
        assertEquals("Software Engineer", jobPost.getTitle());

        // 6. User B finds the Job and sends a Referral Request to User A
        CreateReferralRequest req = new CreateReferralRequest();
        req.setReferrerId(userA.getId());
        req.setCompanyId(company.getId());
        req.setJobIdOrLink("LINK123");
        req.setJobTitle("Software Engineer");
        req.setComments("Please refer me!");

        ReferralRequestDto referralRequest = referralService.createReferralRequest(userB.getEmail(), req);
        assertNotNull(referralRequest);
        assertEquals(ReferralRequestStatus.PENDING, referralRequest.getStatus());

        // 7. User A views incoming requests
        List<ReferralRequestDto> incomingRequests = referralService.getIncomingRequests(userA.getEmail());
        assertEquals(1, incomingRequests.size());
        assertEquals("User B", incomingRequests.get(0).getRequesterName());
        assertEquals("Software Engineer", incomingRequests.get(0).getJobTitle());

        // 8. User A accepts the request
        referralService.approveRequest(userA.getEmail(), incomingRequests.get(0).getId());

        // 9. User B checks outgoing requests and sees it's approved
        ReferralRequest verifiedReq = referralRequestRepository.findById(referralRequest.getId()).get();
        assertEquals(ReferralRequestStatus.ACCEPTED, verifiedReq.getStatus());
    }
}
