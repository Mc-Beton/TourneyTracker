package com.tourney.service.user;

import com.common.domain.User;
import com.common.domain.UserRole;
import com.tourney.dto.user.UserDTO;
import com.tourney.dto.user.UserRegistrationDTO;
import com.tourney.mapper.user.UserMapper;
import com.tourney.repository.user.UserRepository;
import com.tourney.repository.user.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserRegistrationDTO registrationDTO;
    private UserRole participantRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        testUserDTO = UserDTO.builder()
                .id(1L)
                .name("Test User")
                .build();

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setName("New User");
        registrationDTO.setEmail("newuser@example.com");
        registrationDTO.setPassword("password123");

        participantRole = new UserRole();
        participantRole.setId(1L);
        participantRole.setName("PARTICIPANT");
    }

    // ===== FIND BY ID TESTS =====

    @Test
    void testFindById_UserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // When
        UserDTO result = userService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getName(), result.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void testFindById_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class, () -> userService.findById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    // ===== CREATE TESTS =====

    @Test
    void testCreate_NewUser() {
        // Given
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");

        when(userMapper.toEntity(testUserDTO)).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // When
        UserDTO result = userService.create(testUserDTO);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toEntity(testUserDTO);
    }

    @Test
    void testCreate_EnsuresIdIsNull() {
        // Given
        User userWithId = new User();
        userWithId.setId(999L);
        
        when(userMapper.toEntity(testUserDTO)).thenReturn(userWithId);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // When
        userService.create(testUserDTO);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getId(), "ID should be set to null before saving");
    }

    // ===== FIND ALL TESTS =====

    @Test
    void testFindAll_MultipleUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        UserDTO userDTO2 = UserDTO.builder()
                .id(2L)
                .build();

        List<User> users = Arrays.asList(testUser, user2);
        
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);
        when(userMapper.toDto(user2)).thenReturn(userDTO2);

        // When
        List<UserDTO> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_EmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<UserDTO> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    // ===== UPDATE TESTS =====

    @Test
    void testUpdate_ExistingUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toEntity(testUserDTO)).thenReturn(updatedUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(testUserDTO);

        // When
        UserDTO result = userService.update(1L, testUserDTO);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class, () -> userService.update(999L, testUserDTO));
        verify(userRepository, never()).save(any());
    }

    // ===== DELETE TESTS =====

    @Test
    void testDelete_ExistingUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.delete(1L);

        // Then
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // Then
        assertThrows(EntityNotFoundException.class, () -> userService.delete(999L));
        verify(userRepository, never()).deleteById(any());
    }

    // ===== REGISTER USER TESTS =====

    @Test
    void testRegisterUser_Success() {
        // Given
        when(userRepository.findByEmail(registrationDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationDTO.getPassword())).thenReturn("encodedPassword");
        when(userRoleRepository.findByName("PARTICIPANT")).thenReturn(Optional.of(participantRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(registrationDTO);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode(registrationDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertEquals(registrationDTO.getName(), savedUser.getName());
        assertEquals(registrationDTO.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.findByEmail(registrationDTO.getEmail())).thenReturn(Optional.of(testUser));

        // Then
        assertThrows(RuntimeException.class, () -> userService.registerUser(registrationDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_WithRoleAssignment() {
        // Given
        when(userRepository.findByEmail(registrationDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRoleRepository.findByName("PARTICIPANT")).thenReturn(Optional.of(participantRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.registerUser(registrationDTO);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertNotNull(savedUser.getRoles());
        assertTrue(savedUser.getRoles().contains(participantRole));
    }

    @Test
    void testRegisterUser_NoRoleFound_CreatesUserWithoutRole() {
        // Given
        when(userRepository.findByEmail(registrationDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRoleRepository.findByName("PARTICIPANT")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.registerUser(registrationDTO);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertNotNull(savedUser.getRoles());
        assertTrue(savedUser.getRoles().isEmpty());
    }
}
