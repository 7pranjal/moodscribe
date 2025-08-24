package com.example.JournalWebApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmotionAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(EmotionAnalysisService.class);
    private static final String ML_API_URL = "http://localhost:8000/predict";

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> analyzeEmotion(String text) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);

            // Create request entity
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                ML_API_URL,
                request,
                HashMap.class
            );

            if (response != null && response.containsKey("dominant_emotion")) {
                logger.info("Emotion analysis successful for text length: {}", text.length());
                return response;
            } else {
                logger.error("Unexpected response format from ML model");
                throw new RuntimeException("Invalid response from emotion analysis service");
            }
        } catch (Exception e) {
            logger.error("Error analyzing emotion: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze emotion: " + e.getMessage());
        }
    }
} 