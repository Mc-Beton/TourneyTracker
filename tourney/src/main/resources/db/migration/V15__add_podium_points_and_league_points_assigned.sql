-- Add podium points configuration to leagues table
ALTER TABLE leagues ADD COLUMN points_first_place INT NOT NULL DEFAULT 5;
ALTER TABLE leagues ADD COLUMN points_second_place INT NOT NULL DEFAULT 3;
ALTER TABLE leagues ADD COLUMN points_third_place INT NOT NULL DEFAULT 1;

-- Add league_points_assigned flag to tournaments table
ALTER TABLE tournaments ADD COLUMN league_points_assigned BOOLEAN DEFAULT FALSE;
