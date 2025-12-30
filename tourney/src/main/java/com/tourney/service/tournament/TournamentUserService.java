package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.scores.Score;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.user.User;
import com.tourney.dto.complex.UserTournamentMatchesDTO;
import com.tourney.dto.matches.MatchDetailsDTO;
import com.tourney.dto.scores.RoundScoreDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentUserService {
    private final TournamentRepository tournamentRepository;
    private final ScoreRepository scoreRepository;
    private final MatchRepository matchRepository;

    public List<UserTournamentMatchesDTO> getUsersMatchesWithScores(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        List<Match> tournamentMatches = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .collect(Collectors.toList());

        // Pobierz wszystkie wyniki dla meczy w turnieju
        List<Score> allScores = scoreRepository.findAllByTournamentId(tournamentId);

        // Grupuj wyniki według użytkownika i rundy meczu
        Map<Long, Map<Long, Long>> roundScores = allScores.stream()
                .collect(Collectors.groupingBy(
                        score -> score.getUser().getId(),
                        Collectors.groupingBy(
                                score -> score.getMatchRound().getId(),
                                Collectors.summingLong(Score::getScore)
                        )
                ));

        return tournament.getParticipants().stream()
                .map(user -> createUserMatchesDTO(user, tournamentMatches, roundScores))
                .collect(Collectors.toList());
    }

    private UserTournamentMatchesDTO createUserMatchesDTO(
            User user,
            List<Match> tournamentMatches,
            Map<Long, Map<Long, Long>> roundScores
    ) {
        List<MatchDetailsDTO> matchDetails = tournamentMatches.stream()
                .filter(match -> match.getPlayer1().getId().equals(user.getId())
                        || match.getPlayer2().getId().equals(user.getId()))
                .map(match -> createMatchDetailsDTO(match, user, roundScores.getOrDefault(user.getId(), Map.of())))
                .collect(Collectors.toList());

        return UserTournamentMatchesDTO.builder()
                .userId(user.getId())
                .userName(user.getName())
                .matches(matchDetails)
                .build();
    }

    private MatchDetailsDTO createMatchDetailsDTO(
            Match match,
            User user,
            Map<Long, Long> userRoundScores
    ) {
        User opponent = match.getPlayer1().getId().equals(user.getId())
                ? match.getPlayer2()
                : match.getPlayer1();

        List<RoundScoreDTO> roundScores = match.getRounds().stream()
                .map(round -> RoundScoreDTO.builder()
                        .roundNumber(round.getRoundNumber())
                        .build())
                .collect(Collectors.toList());

        return MatchDetailsDTO.builder()
                .matchId(match.getId())
                .startTime(match.getStartTime())
                .opponentId(opponent.getId())
                .opponentName(opponent.getName())
                .roundScores(roundScores)
                .build();
    }


    public List<User> getUsersByTournamentId(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .map(Tournament::getParticipants)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));
    }
}