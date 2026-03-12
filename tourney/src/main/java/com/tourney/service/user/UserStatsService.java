package com.tourney.service.user;

import com.common.domain.User;
import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchResult;
import com.tourney.domain.games.MatchStatus;
import com.tourney.dto.user.UserProfileDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get all matches where user participated
        List<Match> matches = matchRepository.findByPlayer1IdOrPlayer2Id(userId, userId);

        // Calculate stats
        int totalMatches = 0;
        int wins = 0;
        int losses = 0;
        int draws = 0;

        for (Match match : matches) {
            // Only count completed matches with confirmed results
            if (match.getStatus() == MatchStatus.COMPLETED && match.isCompleted() && match.getMatchResult() != null) {
                totalMatches++;
                
                MatchResult result = match.getMatchResult();
                Long winnerId = result.getWinnerId();
                
                if (winnerId == null) {
                    // Draw
                    draws++;
                } else if (winnerId.equals(userId)) {
                    // Win
                    wins++;
                } else {
                    // Loss
                    losses++;
                }
            }
        }

        // Calculate win ratio: (wins + 0.5 * draws) / totalMatches
        double winRatio = 0.0;
        if (totalMatches > 0) {
            winRatio = (wins + 0.5 * draws) / totalMatches;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRealName(user.getRealName());
        dto.setSurname(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setTeam(user.getTeam());
        dto.setCity(user.getCity());
        dto.setDiscordNick(user.getDiscordNick());
        dto.setTotalMatches(totalMatches);
        dto.setWins(wins);
        dto.setLosses(losses);
        dto.setDraws(draws);
        dto.setWinRatio(winRatio);

        return dto;
    }
}
