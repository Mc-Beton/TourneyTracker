package com.common.security; // Zmieniony pakiet na wspólny

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.security.Principal;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {
    private final Long id;
    private final String email;

    @Override
    public String getName() {
        return email;
    }
}