package com.tourney.user_service.domain.tableInit;

import com.tourney.user_service.domain.UserRole;
import com.tourney.user_service.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RoleInitializer {
    @Bean
    CommandLineRunner initRoles(UserRoleRepository userRoleRepository) {
        return args -> {
            if (userRoleRepository.count() == 0) {
                UserRole participant = new UserRole(null, "PARTICIPANT");
                UserRole organizer = new UserRole(null, "ORGANIZER");
                userRoleRepository.saveAll(List.of(participant, organizer));
            }
        };
    }
}
