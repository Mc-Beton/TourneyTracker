package com.tourney.user_service.security;

import com.common.security.UserPrincipal;
import com.tourney.user_service.domain.User;
import com.tourney.user_service.repository.UserRepository;
import com.tourney.user_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null && jwtUtil.isTokenValid(token)) {
                String email = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token); // nowa metoda w JwtUtil
                Long userId = jwtUtil.extractUserId(token);      // nowa metoda w JwtUtil

                if (email != null) {
                    // Tworzymy Principal, który zawiera ID i Email
                    UserPrincipal principal = new UserPrincipal(userId, email);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        } catch (Exception e) {
            logger.error("Nie można ustawić uwierzytelnienia użytkownika: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/register") ||
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