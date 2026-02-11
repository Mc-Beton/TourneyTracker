package com.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // ===== VALID TOKEN TESTS =====

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String username = "test@example.com";
        Long userId = 1L;

        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(jwtService.extractUserId(validToken)).thenReturn(userId);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof UserPrincipal);
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals(username, principal.getEmail());
        assertEquals(userId, principal.getId());
        
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ValidToken_HasAuthority() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        
        when(request.getServletPath()).thenReturn("/api/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn("user@test.com");
        when(jwtService.extractUserId(validToken)).thenReturn(1L);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(1, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    // ===== INVALID TOKEN TESTS =====

    @Test
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).extractUserId(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).isTokenValid(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_MalformedAuthHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).isTokenValid(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ===== SHOULD NOT FILTER TESTS =====

    @Test
    void testShouldNotFilter_AuthEndpoint_ReturnsTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/auth/login");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void testShouldNotFilter_SwaggerEndpoint_ReturnsTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void testShouldNotFilter_ApiDocsEndpoint_ReturnsTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/v3/api-docs");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void testShouldNotFilter_ErrorEndpoint_ReturnsTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/error");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void testShouldNotFilter_ProtectedEndpoint_ReturnsFalse() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/api/tournaments");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertFalse(result);
    }

    // ===== EDGE CASES =====

    @Test
    void testDoFilterInternal_ExceptionDuringProcessing_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.isTokenValid(any())).thenThrow(new RuntimeException("Token processing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NullUsername_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testExtractToken_BearerPrefix() throws ServletException, IOException {
        // Given
        String token = "my.jwt.token";
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user@test.com");
        when(jwtService.extractUserId(token)).thenReturn(1L);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).isTokenValid(token);
    }

    @Test
    void testDoFilterInternal_AlreadyAuthenticated_SkipsAuthentication() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        
        // Set existing authentication
        UserPrincipal existingPrincipal = new UserPrincipal(999L, "existing@test.com");
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingPrincipal, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getServletPath()).thenReturn("/api/tournaments");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn("user@test.com");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertEquals("existing@test.com", principal.getEmail()); // Still the existing user
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
