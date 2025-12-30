package com.common.security;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token);
    String generateToken(String username);
}