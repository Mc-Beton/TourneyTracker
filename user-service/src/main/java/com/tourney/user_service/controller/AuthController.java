package com.tourney.user_service.controller;

import com.tourney.user_service.domain.dto.LoginDTO;
import com.tourney.user_service.domain.dto.UserRegistrationDTO;
import com.tourney.user_service.services.AuthService;
import com.tourney.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(summary = "Rejestracja nowego użytkownika",
            description = "Tworzy nowego użytkownika z podanymi danymi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik został pomyślnie zarejestrowany"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane lub email już istnieje")
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);
        return ResponseEntity.ok("User registered successfully!");
    }

    @Operation(summary = "Logowanie użytkownika",
            description = "Loguje użytkownika i zwraca token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik został pomyślnie zalogowany"),
            @ApiResponse(responseCode = "401", description = "Nieprawidłowe dane logowania")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        return ResponseEntity.ok(token);
    }
}
