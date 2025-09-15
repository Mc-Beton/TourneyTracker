package com.tourney.mapper.scores;

import com.tourney.domain.scores.Score;
import com.tourney.dto.scores.ScoreDTO;
import org.springframework.stereotype.Component;

@Component
public class ScoreMapper {

    public ScoreDTO toDto(Score score) {
        if (score == null) {
            return null;
        }

        return ScoreDTO.builder()
                .id(score.getId())
                .matchRoundId(score.getMatchRound() != null ? score.getMatchRound().getId() : null)
                .userId(score.getUser() != null ? score.getUser().getId() : null)
                .scoreType(score.getScoreType())
                .score(score.getScore())
                .build();
    }

    public Score toEntity(ScoreDTO dto) {
        if (dto == null) {
            return null;
        }

        Score score = new Score();
        score.setId(dto.getId());
        score.setScoreType(dto.getScoreType());
        score.setScore(dto.getScore());
        return score;
    }
}