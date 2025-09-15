package com.tourney.repository.games;

import com.tourney.domain.games.Match;
import com.tourney.domain.user.User;
import com.tourney.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ComponentScan(basePackages = "com.tourney.repository.games")
class MatchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MatchRepository matchRepository;

    private User createValidUser(String name) {
        User user = new User();
        user.setName(name);
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.PARTICIPANT);
        user.setRoles(roles);
        return user;
    }

    private Match createValidMatch(LocalDateTime startTime) {
        Match match = new Match();
        match.setStartTime(startTime);
        match.setGameDurationMinutes(30);
        match.setResultSubmissionDeadline(startTime.plusMinutes(60));
        return match;
    }

    @Test
    void shouldFindMatchesByTimeRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Match match1 = createValidMatch(now.plusHours(1));
        Match match2 = createValidMatch(now.plusHours(2));

        entityManager.persist(match1);
        entityManager.persist(match2);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Match> matches = matchRepository.findByStartTimeBetween(
            now,
            now.plusHours(3)
        );

        // Then
        assertEquals(2, matches.size());
    }

    @Test
    void shouldFindMatchesByPlayers() {
        // Given
        User player1 = createValidUser("Player 1");
        entityManager.persist(player1);

        Match match = createValidMatch(LocalDateTime.now());
        match.setPlayer1(player1);
        entityManager.persist(match);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Match> matches = matchRepository.findByPlayer1IdOrPlayer2Id(
            player1.getId(),
            player1.getId()
        );

        // Then
        assertFalse(matches.isEmpty());
        assertEquals(player1.getId(), matches.get(0).getPlayer1().getId());
    }

    @Test
    void shouldNotFindMatchesOutsideTimeRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Match match = createValidMatch(now.plusHours(4));
        entityManager.persist(match);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Match> matches = matchRepository.findByStartTimeBetween(
            now,
            now.plusHours(3)
        );

        // Then
        assertTrue(matches.isEmpty());
    }
}