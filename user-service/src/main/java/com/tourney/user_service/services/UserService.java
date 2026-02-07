package com.tourney.user_service.services;

import com.common.domain.User;
import com.common.domain.UserRole;
import com.tourney.user_service.domain.dto.UserRegistrationDTO;
import com.tourney.user_service.repository.UserRepository;
import com.tourney.user_service.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use!");
        }

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        // Pobierz domyślną rolę z bazy danych
        Optional<UserRole> participantRole = userRoleRepository.findByName("PARTICIPANT");
        Set<UserRole> roles = new HashSet<>();
        participantRole.ifPresent(roles::add);

        user.setRoles(roles);

        return userRepository.save(user);
    }
}
