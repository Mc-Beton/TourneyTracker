package com.tourney.service.games;

import com.tourney.domain.games.Match;
import com.tourney.dto.games.MatchDTO;
import com.tourney.mapper.games.MatchMapper;
import com.tourney.repository.games.MatchRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    @Transactional(readOnly = true)
    public MatchDTO findById(Long id) {
        return matchRepository.findById(id)
                .map(matchMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public MatchDTO create(MatchDTO matchDTO) {
        Match match = matchMapper.toEntity(matchDTO);
        match.setId(null);
        return matchMapper.toDto(matchRepository.save(match));
    }

    @Transactional(readOnly = true)
    public List<MatchDTO> findAll() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toDto)
                .collect(Collectors.toList());
    }
}