package com.devoteeportal.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${app.keepalive.url:${RENDER_EXTERNAL_URL:http://localhost:8080}/api/health}")
    private String keepAliveUrl;

    public KeepAliveScheduler() {
        this.restTemplate = new RestTemplate();
    }

    // Runs every 5 minutes (300000 milliseconds)
    @Scheduled(fixedRate = 300000)
    public void pingKeepAlive() {
        try {
            logger.info("Pinging keep-alive URL: {}", keepAliveUrl);
            String response = restTemplate.getForObject(keepAliveUrl, String.class);
            logger.info("Keep-alive ping successful. Response: {}", response);
        } catch (Exception e) {
            logger.error("Error during keep-alive ping to {}: {}", keepAliveUrl, e.getMessage());
        }
    }
}
