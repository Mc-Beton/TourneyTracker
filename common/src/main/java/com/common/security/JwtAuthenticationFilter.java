package com.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        log.debug("JwtAuthenticationFilter processing: {} {}", request.getMethod(), path);
        
        try {
            String token = extractToken(request);
            log.debug("Extracted token: {}", token != null ? "present (length=" + token.length() + ")" : "null");

            if (token != null) {
                boolean isValid = jwtService.isTokenValid(token);
                log.debug("Token valid: {}", isValid);
                
                if (isValid) {
                    String username = jwtService.extractUsername(token);
                    Long userId = jwtService.extractUserId(token);
                    log.debug("Extracted username: {}, userId: {}", username, userId);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserPrincipal principal = new UserPrincipal(userId, username);
                        log.debug("Creating authentication for user: {} (id={})", username, userId);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentication set in SecurityContext");
                    } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        log.debug("Authentication already present in SecurityContext");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Błąd uwierzytelniania: ", e);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/") ||
               path.startsWith("/api/users/auth/") ||  // user-service auth endpoints
               path.contains("/auth/") ||  // any auth endpoints
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/error");
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}