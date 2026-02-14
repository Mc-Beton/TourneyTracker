package com.tourney.user_service.services;

import com.common.domain.User;
import com.common.domain.UserRole;
import com.tourney.user_service.domain.dto.UpdateProfileDTO;
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

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Long userId, UpdateProfileDTO updateDTO) {
        User user = getUserById(userId);

        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use!");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getRealName() != null) {
            user.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getSurname() != null) {
            user.setSurname(updateDTO.getSurname());
        }
        if (updateDTO.getBeginner() != null) {
            user.setBeginner(updateDTO.getBeginner());
        }
        if (updateDTO.getTeam() != null) {
            user.setTeam(updateDTO.getTeam());
        }
        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }
        if (updateDTO.getDiscordNick() != null) {
            user.setDiscordNick(updateDTO.getDiscordNick());
        }

        return userRepository.save(user);
    }
}
