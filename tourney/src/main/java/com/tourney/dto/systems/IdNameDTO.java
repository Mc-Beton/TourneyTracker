package com.tourney.dto.systems;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdNameDTO {
    Long id;
    String name;
}