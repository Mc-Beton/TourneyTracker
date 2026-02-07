package com.common.security;

import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token);
    String generateToken(String username);
    String generateToken(Long userId, String email, List<String> roles);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    Long extractUserId(String token);
    List<String> extractRoles(String token);
}