package com.tourney.user_service.domain.dto;

import com.common.domain.User;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String realName;
    private String surname;
    private Boolean beginner;
    private String team;
    private String city;
    private String discordNick;

    public static UserProfileDTO fromUser(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .realName(user.getRealName())
                .surname(user.getSurname())
                .beginner(user.getBeginner())
                .team(user.getTeam())
                .city(user.getCity())
                .discordNick(user.getDiscordNick())
                .build();
    }
}
