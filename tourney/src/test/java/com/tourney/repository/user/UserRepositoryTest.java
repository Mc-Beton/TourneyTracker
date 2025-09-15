package com.tourney.repository.user;

import com.tourney.domain.user.User;
import com.tourney.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ComponentScan(basePackages = "com.tourney.repository.user")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.PARTICIPANT);

        User user = new User();
        user.setName("John Doe");
        user.setRoles(roles);

        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertTrue(foundUser.get().getRoles().contains(UserRole.PARTICIPANT));
    }

    @Test
    void shouldFindByName() {
        // Given
        User user = new User();
        user.setName("Jane Doe");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByName("Jane Doe");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("Jane Doe", foundUser.get().getName());
    }
}