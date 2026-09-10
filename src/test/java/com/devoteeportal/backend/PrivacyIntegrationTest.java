package com.devoteeportal.backend;

import com.devoteeportal.backend.dto.ResumeRequest;
import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.ResumeStatus;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import com.devoteeportal.backend.repository.ContactInfoRequestRepository;
import com.devoteeportal.backend.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PrivacyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactInfoRequestRepository contactInfoRequestRepository;
    
    @Autowired
    private ResumeService resumeService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        contactInfoRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testResumeHiddenFromPublicSearch() throws Exception {
        // 1. Signup User
        String signupJson = "{" +
                "\"email\":\"resume@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Resume User\"," +
                "\"role\":\"USER\"," +
                "\"hideEmployer\":false," +
                "\"chantingRounds\":16," +
                "\"phone\":\"5551234567\"," +
                "\"connectedToContact\":\"0987654321\"" +
                "}";
        
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson))
                .andExpect(status().isOk());
                
        // Approve User
        User user = userRepository.findByEmail("resume@example.com").get();
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(user);

        // Login
        String loginJson = "{\"email\":\"resume@example.com\",\"password\":\"password\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
                
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // 2. Create Resume with hiddenFromPublicSearch = true
        ResumeRequest resumeRequest = new ResumeRequest();
        resumeRequest.setTitle("My Hidden Resume");
        resumeRequest.setStatus(ResumeStatus.ACTIVELY_LOOKING);
        resumeRequest.setHiddenFromPublicSearch(true);
        
        mockMvc.perform(post("/api/resumes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resumeRequest)))
                .andExpect(status().isOk());

        // 3. Confirm it's absent from /api/resumes/browse
        mockMvc.perform(get("/api/resumes/browse")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty()); // Should be empty
    }

    @Test
    void testHideEmployer() throws Exception {
        // 1. Signup User 1 with hideEmployer = true
        String signupJson1 = "{" +
                "\"email\":\"hiddenemp@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Hidden User\"," +
                "\"role\":\"USER\"," +
                "\"hideEmployer\":true," +
                "\"currentEmployer\":\"Secret Corp\"," +
                "\"chantingRounds\":16," +
                "\"phone\":\"5551234567\"," +
                "\"connectedToContact\":\"0987654321\"" +
                "}";
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupJson1)).andExpect(status().isOk());
        User user1 = userRepository.findByEmail("hiddenemp@example.com").get();
        user1.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(user1);
        
        // 2. Signup User 2
        String signupJson2 = "{" +
                "\"email\":\"viewer@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Viewer\"," +
                "\"role\":\"USER\"," +
                "\"hideEmployer\":false," +
                "\"chantingRounds\":16," +
                "\"phone\":\"5551234567\"," +
                "\"connectedToContact\":\"0987654321\"" +
                "}";
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupJson2)).andExpect(status().isOk());
        User user2 = userRepository.findByEmail("viewer@example.com").get();
        user2.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(user2);

        // Login User 2
        String loginJson2 = "{\"email\":\"viewer@example.com\",\"password\":\"password\"}";
        MvcResult loginResult2 = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson2)).andExpect(status().isOk()).andReturn();
        String token2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString()).get("token").asText();

        // 3. User 2 views User 1's profile -> currentEmployer should be absent
        mockMvc.perform(get("/api/profile/" + user1.getId())
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentEmployer").doesNotExist());
                
        // Login User 1
        String loginJson1 = "{\"email\":\"hiddenemp@example.com\",\"password\":\"password\"}";
        MvcResult loginResult1 = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson1)).andExpect(status().isOk()).andReturn();
        String token1 = objectMapper.readTree(loginResult1.getResponse().getContentAsString()).get("token").asText();
        
        // 4. User 1 views own profile -> currentEmployer should be present
        mockMvc.perform(get("/api/profile/me")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentEmployer").value("Secret Corp"));
    }
}
