CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    abbreviation VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    game_system_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_team_game_system FOREIGN KEY (game_system_id) REFERENCES gamesystems (id)
);

CREATE TABLE team_members (
    id SERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_team_member UNIQUE (team_id, user_id)
);
