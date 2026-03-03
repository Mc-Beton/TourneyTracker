package com.tourney.service.tournament;

import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.ParticipantStatsDTO;
import com.tourney.dto.tournament.PodiumDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serwis do pobierania statystyk uczestników turnieju.
 * Statystyki są aktualizowane automatycznie po każdym meczu przez ParticipantStatsUpdateService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentStatsService {

    /**
     * Pobiera statystyki wszystkich uczestników turnieju z bazy danych.
     * Statystyki są aktualizowane automatycznie po każdym zakończonym meczu.
     */
    public List<ParticipantStatsDTO> calculateParticipantStats(Tournament tournament) {
        // Pobierz statystyki z bazy (już przeliczone)
        return tournament.getParticipantLinks().stream()
                .filter(TournamentParticipant::isConfirmed)
                .map(participant -> ParticipantStatsDTO.builder()
                        .userId(participant.getUser().getId())
                        .userName(participant.getUser().getName())
                        .wins(participant.getWins())
                        .draws(participant.getDraws())
                        .losses(participant.getLosses())
                        .tournamentPoints(participant.getTournamentPoints())
                        .additionalPoints(participant.getAdditionalPoints())
                        .scorePoints(participant.getScorePoints())
                        .matchesPlayed(participant.getMatchesPlayed())
                        .build())
                // Sortowanie według TP (malejąco), potem małych punktów (malejąco)
                // Punktacja dodatkowa uwzględniana tylko jeśli turniej zakończony
                .sorted((p1, p2) -> {
                    boolean isCompleted = tournament.getStatus() == com.tourney.dto.tournament.TournamentStatus.COMPLETED;
                    int tp1 = p1.getTournamentPoints() + (isCompleted ? p1.getAdditionalPoints() : 0);
                    int tp2 = p2.getTournamentPoints() + (isCompleted ? p2.getAdditionalPoints() : 0);
                    
                    if (tp1 != tp2) {
                        return Integer.compare(tp2, tp1); // desc
                    }
                    return Long.compare(p2.getScorePoints(), p1.getScorePoints()); // desc
                })
                .collect(Collectors.toList());
    }

    /**
     * Oblicza podium (top 3)
     */
    public PodiumDTO calculatePodium(Tournament tournament) {
        List<ParticipantStatsDTO> stats = calculateParticipantStats(tournament);
        
        return PodiumDTO.builder()
                .first(stats.size() > 0 ? stats.get(0) : null)
                .second(stats.size() > 1 ? stats.get(1) : null)
                .third(stats.size() > 2 ? stats.get(2) : null)
                .build();
    }
}
