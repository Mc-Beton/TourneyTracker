package com.tourney.service.user;

import com.common.domain.User;
import com.common.domain.UserRole;
import com.tourney.dto.user.UserDTO;
import com.tourney.dto.user.UserRegistrationDTO;
import com.tourney.mapper.user.UserMapper;
import com.tourney.repository.user.UserRepository;
import com.tourney.repository.user.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserDTO create(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user.setId(null); // Ensure we're creating a new entity
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO update(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        User updatedUser = userMapper.toEntity(userDTO);
        updatedUser.setId(id);
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
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