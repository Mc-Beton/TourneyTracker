ALTER TABLE tournament_round_definitions
ADD COLUMN avoid_same_team_pairing BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tournament_round_definitions
ADD COLUMN avoid_same_city_pairing BOOLEAN NOT NULL DEFAULT FALSE;
