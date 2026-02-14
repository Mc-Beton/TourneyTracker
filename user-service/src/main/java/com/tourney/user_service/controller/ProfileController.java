package com.tourney.user_service.controller;

import com.common.domain.User;
import com.common.security.JwtService;
import com.tourney.user_service.domain.dto.UpdateProfileDTO;
import com.tourney.user_service.domain.dto.UserProfileDTO;
import com.tourney.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final JwtService jwtService;

    public ProfileController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Pobierz profil użytkownika",
            description = "Zwraca dane profilu zalogowanego użytkownika",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil został pobrany"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserProfileDTO.fromUser(user));
    }

    @Operation(summary = "Aktualizuj profil użytkownika",
            description = "Aktualizuje dane profilu zalogowanego użytkownika",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil został zaktualizowany"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileDTO updateDTO) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        
        User updatedUser = userService.updateProfile(userId, updateDTO);
        return ResponseEntity.ok(UserProfileDTO.fromUser(updatedUser));
    }
}
