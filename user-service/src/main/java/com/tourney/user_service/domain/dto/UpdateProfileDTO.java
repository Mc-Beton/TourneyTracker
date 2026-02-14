package com.tourney.user_service.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDTO {
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String realName;
    
    private String surname;
    
    private Boolean beginner;
    
    private String team;
    
    private String city;
    
    private String discordNick;
}
