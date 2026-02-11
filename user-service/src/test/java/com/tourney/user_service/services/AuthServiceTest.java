package com.tourney.user_service.services;

import com.common.domain.User;
import com.common.domain.UserRole;
import com.common.security.JwtService;
import com.tourney.user_service.domain.dto.LoginDTO;
import com.tourney.user_service.exception.InvalidCredentialsException;
import com.tourney.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginDTO loginDTO;
    private UserRole adminRole;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        adminRole = new UserRole();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");

        userRole = new UserRole();
        userRole.setId(2L);
        userRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(new HashSet<>(Arrays.asList(userRole, adminRole)));

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");
    }

    // ===== LOGIN SUCCESS TESTS =====

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        // Given
        String expectedToken = "jwt.token.here";
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(eq(testUser.getId()), eq(testUser.getEmail()), any())).thenReturn(expectedToken);

        // When
        String token = authService.login(loginDTO);

        // Then
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(userRepository, times(1)).findByEmail(loginDTO.getEmail());
        verify(passwordEncoder, times(1)).matches(loginDTO.getPassword(), testUser.getPassword());
        verify(jwtService, times(1)).generateToken(eq(testUser.getId()), eq(testUser.getEmail()), any());
    }

    @Test
    void testLogin_ValidCredentials_IncludesAllRoles() {
        // Given
        String expectedToken = "jwt.token.here";
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn(expectedToken);

        // When
        authService.login(loginDTO);

        // Then
        verify(jwtService).generateToken(
                eq(testUser.getId()),
                eq(testUser.getEmail()),
                argThat(roles -> roles.size() == 2 && roles.contains("ROLE_ADMIN") && roles.contains("ROLE_USER"))
        );
    }

    @Test
    void testLogin_UserWithSingleRole() {
        // Given
        testUser.setRoles(Collections.singleton(userRole));
        String expectedToken = "jwt.token.here";
        
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn(expectedToken);

        // When
        String token = authService.login(loginDTO);

        // Then
        assertEquals(expectedToken, token);
        verify(jwtService).generateToken(
                eq(testUser.getId()),
                eq(testUser.getEmail()),
                argThat(roles -> roles.size() == 1 && roles.contains("ROLE_USER"))
        );
    }

    // ===== LOGIN FAILURE TESTS =====

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        // Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, 
                () -> authService.login(loginDTO));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(loginDTO.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        // Given
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(false);

        // Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, 
                () -> authService.login(loginDTO));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(loginDTO.getEmail());
        verify(passwordEncoder, times(1)).matches(loginDTO.getPassword(), testUser.getPassword());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void testLogin_WrongEmail_ThrowsException() {
        // Given
        loginDTO.setEmail("wrong@example.com");
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        // Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDTO));
    }

    @Test
    void testLogin_NullPassword_ThrowsException() {
        // Given
        loginDTO.setPassword(null);
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(null, testUser.getPassword())).thenReturn(false);

        // Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDTO));
    }

    // ===== EDGE CASES =====

    @Test
    void testLogin_UserWithNoRoles() {
        // Given
        testUser.setRoles(new HashSet<>());
        String expectedToken = "jwt.token.here";
        
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn(expectedToken);

        // When
        String token = authService.login(loginDTO);

        // Then
        assertEquals(expectedToken, token);
        verify(jwtService).generateToken(
                eq(testUser.getId()),
                eq(testUser.getEmail()),
                argThat(List::isEmpty)
        );
    }

    @Test
    void testLogin_CaseInsensitiveEmail() {
        // Given
        String expectedToken = "jwt.token.here";
        loginDTO.setEmail("TEST@EXAMPLE.COM");
        
        when(userRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn(expectedToken);

        // When
        String token = authService.login(loginDTO);

        // Then
        assertNotNull(token);
        assertEquals(expectedToken, token);
    }

    @Test
    void testLogin_PasswordEncoderCalledWithCorrectParameters() {
        // Given
        String rawPassword = "myRawPassword123";
        String encodedPassword = "encodedHash";
        loginDTO.setPassword(rawPassword);
        testUser.setPassword(encodedPassword);
        
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("token");

        // When
        authService.login(loginDTO);

        // Then
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testLogin_MultipleCalls_IndependentResults() {
        // Given
        String token1 = "token1";
        String token2 = "token2";
        
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any()))
                .thenReturn(token1)
                .thenReturn(token2);

        // When
        String result1 = authService.login(loginDTO);
        String result2 = authService.login(loginDTO);

        // Then
        assertEquals(token1, result1);
        assertEquals(token2, result2);
        verify(userRepository, times(2)).findByEmail(loginDTO.getEmail());
    }
}
