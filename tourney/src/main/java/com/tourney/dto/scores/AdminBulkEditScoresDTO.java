package com.tourney.dto.scores;

import lombok.Data;

import java.util.List;

/**
 * Bulk payload for organizer editing all rounds' scores in one request.
 * Reuses existing SubmitScoreDTO structure: roundNumber + List<ScoreEntryDTO>.
 */
@Data
public class AdminBulkEditScoresDTO {
    private List<SubmitScoreDTO> rounds;
}