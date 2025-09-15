package com.tourney.dto.user;

import com.tourney.domain.user.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private Set<UserRole> roles;
}