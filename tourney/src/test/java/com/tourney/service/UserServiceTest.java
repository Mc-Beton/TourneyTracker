package com.tourney.service;

import com.tourney.domain.user.User;
import com.tourney.dto.user.UserDTO;
import com.tourney.mapper.user.UserMapper;
import com.tourney.repository.user.UserRepository;
import com.tourney.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserDTO userDTO = UserDTO.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDTO);

        // When
        UserDTO result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }
}