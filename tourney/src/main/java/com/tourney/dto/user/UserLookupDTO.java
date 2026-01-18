package com.tourney.dto.user;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserLookupDTO {
    Long id;
    String name;
}