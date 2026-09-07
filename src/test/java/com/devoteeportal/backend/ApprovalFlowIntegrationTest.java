package com.devoteeportal.backend;

import com.devoteeportal.backend.dto.LoginRequest;
import com.devoteeportal.backend.dto.SignupRequest;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.entity.UserStatus;
import com.devoteeportal.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApprovalFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testApprovalFlow() throws Exception {
        // 1. Signup
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("testuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setPhone("1234567890");
        signupRequest.setConnectedToContact("0987654321");

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String userId = JsonPath.read(signupResult.getResponse().getContentAsString(), "$.id");

        // 2. Login Blocked (Pending)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testuser@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());

        // 3. Admin Setup
        SignupRequest adminSignup = new SignupRequest();
        adminSignup.setName("Real Admin");
        adminSignup.setEmail("realadmin@example.com");
        adminSignup.setPassword("adminpass");
        adminSignup.setPhone("1111111112");
        adminSignup.setConnectedToContact("0987654321");
        
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminSignup)))
                .andExpect(status().isOk());

        User realAdmin = userRepository.findByEmail("realadmin@example.com").orElseThrow();
        realAdmin.setRole(Role.ADMIN);
        realAdmin.setStatus(UserStatus.APPROVED);
        userRepository.save(realAdmin);

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("realadmin@example.com");
        adminLogin.setPassword("adminpass");
        
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = JsonPath.read(adminLoginResult.getResponse().getContentAsString(), "$.token");

        // 4. Admin Approves
        mockMvc.perform(put("/api/admin/" + userId + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 5. Login Succeeds
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
