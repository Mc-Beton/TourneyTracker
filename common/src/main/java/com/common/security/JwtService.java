package com.common.security;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token);
    String generateToken(String username);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    Long extractUserId(String token);

}