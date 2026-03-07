-- Add league_id column to tournaments table
ALTER TABLE tournaments ADD COLUMN league_id BIGINT;

-- Add foreign key constraint
ALTER TABLE tournaments ADD CONSTRAINT fk_tournament_league 
    FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE SET NULL;

-- Migrate existing data from league_tournaments to tournaments.league_id
-- Only migrate if there's exactly one league per tournament
UPDATE tournaments t
SET league_id = lt.league_id
FROM league_tournaments lt
WHERE t.id = lt.tournament_id;

-- Drop the league_tournaments table as it's no longer needed
DROP TABLE IF EXISTS league_tournaments;
