CREATE TABLE IF NOT EXISTS leagues (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    game_system_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    auto_accept_games BOOLEAN NOT NULL DEFAULT FALSE,
    auto_accept_tournaments BOOLEAN NOT NULL DEFAULT FALSE,
    points_win INTEGER NOT NULL DEFAULT 3,
    points_draw INTEGER NOT NULL DEFAULT 1,
    points_loss INTEGER NOT NULL DEFAULT 0,
    points_participation INTEGER NOT NULL DEFAULT 1,
    points_per_participant INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_league_game_system FOREIGN KEY (game_system_id) REFERENCES gamesystems (id),
    CONSTRAINT fk_league_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS league_members (
    id SERIAL PRIMARY KEY,
    league_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED
    points INTEGER NOT NULL DEFAULT 0,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_league_member_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_league_member_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_league_member UNIQUE (league_id, user_id)
);

CREATE TABLE IF NOT EXISTS league_tournaments (
    id SERIAL PRIMARY KEY,
    league_id BIGINT NOT NULL,
    tournament_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED
    CONSTRAINT fk_league_tournament_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_league_tournament_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT uq_league_tournament UNIQUE (league_id, tournament_id)
);

CREATE TABLE IF NOT EXISTS league_matches (
    id SERIAL PRIMARY KEY,
    league_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED
    CONSTRAINT fk_league_match_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_league_match_match FOREIGN KEY (match_id) REFERENCES matches (id),
    CONSTRAINT uq_league_match UNIQUE (league_id, match_id)
);
