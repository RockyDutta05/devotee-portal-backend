package com.devoteeportal.backend;

import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.Role;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminSecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testAdminEndpointsSecurity() throws Exception {
        // 1. Setup Non-Admin User
        String signupJson = "{" +
                "\"email\":\"nonadmin@example.com\"," +
                "\"password\":\"password\"," +
                "\"name\":\"Non Admin\"," +
                "\"role\":\"USER\"," +
                "\"chantingRounds\":16," +
                "\"phone\":\"1234567890\"," +
                "\"connectedToContact\":\"0987654321\"" +
                "}";
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupJson)).andExpect(status().isOk());
        User user = userRepository.findByEmail("nonadmin@example.com").get();
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(user);

        // Login to get token
        String loginJson = "{\"email\":\"nonadmin@example.com\",\"password\":\"password\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson)).andExpect(status().isOk()).andReturn();
        String nonAdminToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Array of admin endpoints to check (GET for simplicity)
        String[] adminEndpoints = {
            "/api/admin/users/pending",
            "/api/admin/companies/pending",
            "/api/admin/dashboard-stats",
            "/api/admin/settings"
        };

        for (String endpoint : adminEndpoints) {
            // Test 401 Unauthorized (No Token)
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());

            // Test 403 Forbidden (Non-Admin JWT)
            mockMvc.perform(get(endpoint)
                    .header("Authorization", "Bearer " + nonAdminToken))
                    .andExpect(status().isForbidden());
        }
    }
}
