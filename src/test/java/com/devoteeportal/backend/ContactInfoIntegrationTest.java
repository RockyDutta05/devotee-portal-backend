package com.devoteeportal.backend;

import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import com.devoteeportal.backend.service.NetworkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContactInfoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NetworkingService networkingService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testContactInfoRevealFlow() throws Exception {
        // 1. Setup Target User
        String signupJsonTarget = "{" +
                "\"email\":\"target@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Target User\"," +
                "\"phone\":\"5551234567\"," +
                "\"role\":\"USER\"," +
                "\"chantingRounds\":16" +
                "}";
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupJsonTarget)).andExpect(status().isOk());
        User targetUser = userRepository.findByEmail("target@example.com").get();
        targetUser.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(targetUser);

        // 2. Setup Requester User
        String signupJsonReq = "{" +
                "\"email\":\"req@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Requester\"," +
                "\"role\":\"USER\"," +
                "\"chantingRounds\":16," +
                "\"phone\":\"5559876543\"," +
                "\"connectedToContact\":\"0987654321\"" +
                "}";
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupJsonReq)).andExpect(status().isOk());
        User reqUser = userRepository.findByEmail("req@example.com").get();
        reqUser.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(reqUser);

        // Login Requester
        String loginJsonReq = "{\"email\":\"req@example.com\",\"password\":\"password\"}";
        MvcResult loginResultReq = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJsonReq)).andExpect(status().isOk()).andReturn();
        String tokenReq = objectMapper.readTree(loginResultReq.getResponse().getContentAsString()).get("token").asText();

        // 3. View Profile BEFORE connection
        mockMvc.perform(get("/api/profile/" + targetUser.getId())
                .header("Authorization", "Bearer " + tokenReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").doesNotExist()) // Or null/empty string depending on impl
                .andExpect(jsonPath("$.email").doesNotExist()); // Same

        // 4. Send & Approve Connection Request
        // Using service directly for simplicity in integration test setup
        com.devoteeportal.backend.dto.CreateContactInfoRequest createReq = new com.devoteeportal.backend.dto.CreateContactInfoRequest();
        createReq.setTargetId(targetUser.getId());
        createReq.setReason("Please share your contact info");
        networkingService.createContactInfoRequest(reqUser.getEmail(), createReq);
        
        // Target logs in to approve
        String loginJsonTargetLogin = "{\"email\":\"target@example.com\",\"password\":\"password\"}";
        MvcResult loginResultTarget = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJsonTargetLogin)).andExpect(status().isOk()).andReturn();
        String tokenTarget = objectMapper.readTree(loginResultTarget.getResponse().getContentAsString()).get("token").asText();
        
        // Just approve via service for speed
        var requests = networkingService.getIncomingContactRequests(targetUser.getEmail());
        networkingService.updateContactInfoRequestStatus(targetUser.getEmail(), requests.get(0).getId(), com.devoteeportal.backend.entity.RequestStatus.APPROVED);

        // 5. View Profile AFTER connection
        mockMvc.perform(get("/api/profile/" + targetUser.getId())
                .header("Authorization", "Bearer " + tokenReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("5551234567"))
                .andExpect(jsonPath("$.email").value("target@example.com"));
    }
}
