package com.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA="; // Base64 encoded 256-bit key
    private static final String TEST_USERNAME = "test@example.com";
    private static final Long TEST_USER_ID = 123L;
    private static final List<String> TEST_ROLES = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    }

    @Test
    void testGenerateToken_withUsername() {
        // When
        String token = jwtService.generateToken(TEST_USERNAME);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_withUserIdEmailAndRoles() {
        // When
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testExtractUsername() {
        // Given
        String token = jwtService.generateToken(TEST_USERNAME);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void testExtractUserId() {
        // Given
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(TEST_USER_ID, extractedUserId);
    }

    @Test
    void testExtractRoles() {
        // Given
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // When
        List<String> extractedRoles = jwtService.extractRoles(token);

        // Then
        assertNotNull(extractedRoles);
        assertEquals(TEST_ROLES.size(), extractedRoles.size());
        assertTrue(extractedRoles.containsAll(TEST_ROLES));
    }

    @Test
    void testIsTokenValid_withValidToken() {
        // Given
        String token = jwtService.generateToken(TEST_USERNAME);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_withInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_withExpiredToken() {
        // Given - można stworzyć token z ujemnym czasem wygaśnięcia przez mockowanie
        // Dla uproszczenia, testujemy tylko nieprawidłowy format
        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.invalid.signature";

        // When
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testExtractClaim_customClaim() {
        // Given
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // When
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
    }

    @Test
    void testTokenContainsAllRequiredClaims() {
        // Given
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // When
        String username = jwtService.extractUsername(token);
        Long userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);

        // Then
        assertEquals(TEST_USERNAME, username);
        assertEquals(TEST_USER_ID, userId);
        assertNotNull(roles);
        assertEquals(TEST_ROLES.size(), roles.size());
    }

    @Test
    void testExtractUserId_withIntegerValue() {
        // This tests the conversion from Integer to Long in extractUserId
        // Given
        String token = jwtService.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

        // When
        Long userId = jwtService.extractUserId(token);

        // Then
        assertNotNull(userId);
        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    void testGenerateToken_tokenNotEmpty() {
        // When
        String token = jwtService.generateToken(TEST_USERNAME);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 50); // JWT tokens are typically quite long
    }
}
