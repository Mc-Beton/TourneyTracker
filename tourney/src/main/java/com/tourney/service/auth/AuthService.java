package com.tourney.service.auth;

import com.common.domain.User;
import com.common.security.JwtService;
import com.tourney.dto.user.LoginDTO;
import com.tourney.exception.InvalidCredentialsException;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginDTO loginDTO) {
        Optional<User> userOptional = userRepository.findByEmail(loginDTO.getEmail());
        if (userOptional.isEmpty() || !passwordEncoder.matches(loginDTO.getPassword(), userOptional.get().getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        User user = userOptional.get();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        
        return jwtService.generateToken(user.getId(), user.getEmail(), roles);
    }
}
