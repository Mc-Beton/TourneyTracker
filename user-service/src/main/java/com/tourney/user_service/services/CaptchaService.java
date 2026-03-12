package com.tourney.user_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class CaptchaService {

    @Value("${google.recaptcha.secret-key}")
    private String secretKey;

    @Value("${google.recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Verifies the CAPTCHA token with Google's reCAPTCHA API
     * @param captchaToken The token received from the frontend
     * @return true if CAPTCHA is valid, false otherwise
     */
    public boolean verifyCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", captchaToken);

            String response = restTemplate.postForObject(verifyUrl, params, String.class);
            
            if (response == null) {
                return false;
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("success").asBoolean();
        } catch (Exception e) {
            // Log the error in production
            System.err.println("CAPTCHA verification failed: " + e.getMessage());
            return false;
        }
    }
}
