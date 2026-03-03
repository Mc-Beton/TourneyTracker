-- Teams table
CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    abbreviation VARCHAR(10) NOT NULL,
    city VARCHAR(255),
    description TEXT,
    game_system_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_teams_game_system FOREIGN KEY (game_system_id) REFERENCES game_systems(id),
    CONSTRAINT fk_teams_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Team Members table
CREATE TABLE IF NOT EXISTS team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, ACTIVE, INACTIVE
    joined_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_team_member UNIQUE (team_id, user_id)
);

-- Index for searching teams by game system
CREATE INDEX IF NOT EXISTS idx_teams_game_system ON teams(game_system_id);
-- Index for finding user's teams
CREATE INDEX IF NOT EXISTS idx_team_members_user ON team_members(user_id);
