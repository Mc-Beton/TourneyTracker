package com.tourney.service.tournament;

import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final TournamentMapper tournamentMapper;

    public List<ActiveTournamentDTO> getActiveTournaments(Long playerId) {
        return tournamentRepository.findActiveForPlayer(playerId).stream()
                .map(tournament -> tournamentMapper.toActiveDTO((Tournament) tournament, playerId))
                .collect(Collectors.toList());
    }
}