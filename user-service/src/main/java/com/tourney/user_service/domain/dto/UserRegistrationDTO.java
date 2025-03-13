package com.tourney.user_service.domain.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    private String name;
    private String email;
    private String password;
}
