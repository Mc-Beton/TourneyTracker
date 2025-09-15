package com.tourney.domain.user;

import lombok.Getter;

@Getter
public enum UserRole {

    PARTICIPANT(2),
    ORGANIZER(1);

    private final int typeId;

    UserRole(int typeId) {
        this.typeId = typeId;
    }

}
