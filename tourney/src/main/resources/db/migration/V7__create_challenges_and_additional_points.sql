-- Skrypt tworzący tabelę dla wyzwań w turnieju (TournamentChallenge)

CREATE TABLE IF NOT EXISTS tournament_challenges (
    id BIGSERIAL PRIMARY KEY,
    challenger_id BIGINT NOT NULL,
    opponent_id BIGINT NOT NULL,
    tournament_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_challenge_challenger FOREIGN KEY (challenger_id) REFERENCES users(id),
    CONSTRAINT fk_challenge_opponent FOREIGN KEY (opponent_id) REFERENCES users(id),
    CONSTRAINT fk_challenge_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id)
);

-- Indeksy dla wydajności przy wyszukiwaniu wyzwań
CREATE INDEX IF NOT EXISTS idx_challenge_tournament_status ON tournament_challenges(tournament_id, status);
CREATE INDEX IF NOT EXISTS idx_challenge_challenger ON tournament_challenges(challenger_id);
CREATE INDEX IF NOT EXISTS idx_challenge_opponent ON tournament_challenges(opponent_id);

-- Dodanie kolumny additional_points do tabeli tournament_participants
ALTER TABLE tournament_participants ADD COLUMN IF NOT EXISTS additional_points INTEGER DEFAULT 0 NOT NULL;
