package com.tourney.user_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

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
            log.warn("CAPTCHA token is null or empty");
            return false;
        }

        try {
            log.info("Verifying CAPTCHA token with Google...");
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", captchaToken);

            String response = restTemplate.postForObject(verifyUrl, params, String.class);
            
            log.info("Google reCAPTCHA response: {}", response);
            
            if (response == null) {
                log.error("Google reCAPTCHA returned null response");
                return false;
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            boolean success = jsonNode.get("success").asBoolean();
            
            if (!success) {
                JsonNode errorCodes = jsonNode.get("error-codes");
                log.error("CAPTCHA verification failed. Error codes: {}", errorCodes);
            } else {
                log.info("CAPTCHA verification successful");
            }
            
            return success;
        } catch (Exception e) {
            log.error("CAPTCHA verification exception: {}", e.getMessage(), e);
            return false;
        }
    }
}
