package com.tourney.mapper.user;

import com.common.domain.User;
import com.tourney.dto.user.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .roles(user.getRoles())
                .build();
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setRoles(dto.getRoles());
        return user;
    }
}