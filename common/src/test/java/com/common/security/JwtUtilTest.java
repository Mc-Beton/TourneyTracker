package com.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "M2Y0ZGRhN2Y5M2U0YTdmMDM0ZDMwOTY0MzFlYjA4Y2U1ZGNhOTY3YzVlYmU1ZDAzYmE3OTU1ODIxMTExOTFhYg==";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken() {
        // When
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // When
        String extractedEmail = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(TEST_EMAIL, extractedEmail);
    }

    @Test
    void testValidateToken_withValidToken() {
        // Given
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_withInvalidToken() {
        // Given
        String invalidToken = "invalid.token.value";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_withMalformedToken() {
        // Given
        String malformedToken = "malformed-token-without-dots";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_withEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        Boolean isValid = jwtUtil.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_withNullToken() {
        // Given
        String nullToken = null;

        // When
        Boolean isValid = jwtUtil.validateToken(nullToken);

        // Then - Should return false instead of throwing exception
        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_differentEmailsProduceDifferentTokens() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // When
        String token1 = jwtUtil.generateToken(email1);
        String token2 = jwtUtil.generateToken(email2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void testGenerateToken_consecutiveCalls_ProducesSimilarTokensIfTimestampNotChanged() throws InterruptedException {
        // Given - JWT tokens with same data and close timestamps might be identical

        // When
        String token1 = jwtUtil.generateToken(TEST_EMAIL);
        Thread.sleep(1000); // 1 second delay to ensure different timestamp
        String token2 = jwtUtil.generateToken(TEST_EMAIL);

        // Then
        // Tokens might be the same if timestamps are too close,
        // but both should be valid tokens
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
    }

    @Test
    void testGetUsernameFromToken_withDifferentEmails() {
        // Given
        String email1 = "john@example.com";
        String email2 = "jane@example.com";
        String token1 = jwtUtil.generateToken(email1);
        String token2 = jwtUtil.generateToken(email2);

        // When
        String extractedEmail1 = jwtUtil.getUsernameFromToken(token1);
        String extractedEmail2 = jwtUtil.getUsernameFromToken(token2);

        // Then
        assertEquals(email1, extractedEmail1);
        assertEquals(email2, extractedEmail2);
        assertNotEquals(extractedEmail1, extractedEmail2);
    }

    @Test
    void testGeneratedTokenIsNotEmpty() {
        // When
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 50); // JWT tokens are typically long
    }

    @Test
    void testValidateToken_withTamperedToken() {
        // Given
        String validToken = jwtUtil.generateToken(TEST_EMAIL);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // When
        Boolean isValid = jwtUtil.validateToken(tamperedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testTokenStructure() {
        // When
        String token = jwtUtil.generateToken(TEST_EMAIL);
        String[] parts = token.split("\\.");

        // Then
        assertEquals(3, parts.length, "JWT should have 3 parts");
        assertTrue(parts[0].length() > 0, "Header should not be empty");
        assertTrue(parts[1].length() > 0, "Payload should not be empty");
        assertTrue(parts[2].length() > 0, "Signature should not be empty");
    }
}
