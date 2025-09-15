
package com.tourney.mapper;

import com.tourney.domain.user.User;
import com.tourney.domain.user.UserRole;
import com.tourney.dto.user.UserDTO;
import com.tourney.mapper.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void shouldMapUserToDto() {
        // Given
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.PARTICIPANT);

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setRoles(roles);

        // When
        UserDTO dto = userMapper.toDto(user);

        // Then
        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getRoles(), dto.getRoles());
    }

    @Test
    void shouldMapDtoToUser() {
        // Given
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ORGANIZER);

        UserDTO dto = UserDTO.builder()
                .id(1L)
                .name("John Doe")
                .roles(roles)
                .build();

        // When
        User user = userMapper.toEntity(dto);

        // Then
        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getRoles(), user.getRoles());
    }
}