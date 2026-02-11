package com.tourney.user_service.controller;

import com.tourney.user_service.domain.dto.LoginDTO;
import com.tourney.user_service.domain.dto.UserRegistrationDTO;
import com.tourney.user_service.services.AuthService;
import com.tourney.user_service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserRegistrationDTO registrationDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setName("Test User");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");
    }

    // ===== REGISTER TESTS =====

    @Test
    void testRegisterUser_Success() {
        // Given
        when(userService.registerUser(registrationDTO)).thenReturn(null); // Returns User but we don't use it

        // When
        ResponseEntity<String> response = authController.registerUser(registrationDTO);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully!", response.getBody());
        verify(userService, times(1)).registerUser(registrationDTO);
    }

    @Test
    void testRegisterUser_ValidDTO() {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(null);

        // When
        ResponseEntity<String> response = authController.registerUser(registrationDTO);

        // Then
        verify(userService).registerUser(argThat(dto ->
                dto.getName().equals("Test User") &&
                dto.getEmail().equals("test@example.com") &&
                dto.getPassword().equals("password123")
        ));
    }

    @Test
    void testRegisterUser_ServiceThrowsException_PropagatesException() {
        // Given
        doThrow(new RuntimeException("Email already in use"))
                .when(userService).registerUser(registrationDTO);

        // Then
        assertThrows(RuntimeException.class, () -> authController.registerUser(registrationDTO));
    }

    // ===== LOGIN TESTS =====

    @Test
    void testLogin_Success_ReturnsToken() {
        // Given
        String expectedToken = "jwt.token.here";
        when(authService.login(loginDTO)).thenReturn(expectedToken);

        // When
        ResponseEntity<String> response = authController.login(loginDTO);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedToken, response.getBody());
        verify(authService, times(1)).login(loginDTO);
    }

    @Test
    void testLogin_ValidCredentials() {
        // Given
        String token = "valid.jwt.token";
        when(authService.login(any(LoginDTO.class))).thenReturn(token);

        // When
        ResponseEntity<String> response = authController.login(loginDTO);

        // Then
        verify(authService).login(argThat(dto ->
                dto.getEmail().equals("test@example.com") &&
                dto.getPassword().equals("password123")
        ));
    }

    @Test
    void testLogin_InvalidCredentials_ServiceThrowsException() {
        // Given
        when(authService.login(loginDTO))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Then
        assertThrows(RuntimeException.class, () -> authController.login(loginDTO));
    }

    @Test
    void testLogin_DifferentTokens() {
        // Given
        String token1 = "token1";
        String token2 = "token2";
        
        LoginDTO login1 = new LoginDTO();
        login1.setEmail("user1@example.com");
        login1.setPassword("pass1");
        
        LoginDTO login2 = new LoginDTO();
        login2.setEmail("user2@example.com");
        login2.setPassword("pass2");

        when(authService.login(login1)).thenReturn(token1);
        when(authService.login(login2)).thenReturn(token2);

        // When
        ResponseEntity<String> response1 = authController.login(login1);
        ResponseEntity<String> response2 = authController.login(login2);

        // Then
        assertEquals(token1, response1.getBody());
        assertEquals(token2, response2.getBody());
    }

    // ===== INTEGRATION TESTS =====

    @Test
    void testRegisterAndLogin_Workflow() {
        // Given
        String expectedToken = "new.user.token";
        when(userService.registerUser(registrationDTO)).thenReturn(null);
        when(authService.login(any(LoginDTO.class))).thenReturn(expectedToken);

        // When - Register
        ResponseEntity<String> registerResponse = authController.registerUser(registrationDTO);
        
        // Then - Registration successful
        assertEquals("User registered successfully!", registerResponse.getBody());
        verify(userService, times(1)).registerUser(registrationDTO);

        // When - Login
        ResponseEntity<String> loginResponse = authController.login(loginDTO);
        
        // Then - Login successful
        assertEquals(expectedToken, loginResponse.getBody());
        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    void testLogin_ReturnsOkStatus() {
        // Given
        when(authService.login(any())).thenReturn("token");

        // When
        ResponseEntity<String> response = authController.login(loginDTO);

        // Then
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testRegisterUser_ReturnsOkStatus() {
        // Given
        when(userService.registerUser(any())).thenReturn(null);

        // When
        ResponseEntity<String> response = authController.registerUser(registrationDTO);

        // Then
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
