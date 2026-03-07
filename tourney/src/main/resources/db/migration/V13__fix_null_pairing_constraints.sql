-- Fix NULL values in avoid_same_team_pairing and avoid_same_city_pairing columns
UPDATE tournament_round_definitions
SET avoid_same_team_pairing = FALSE
WHERE avoid_same_team_pairing IS NULL;

UPDATE tournament_round_definitions
SET avoid_same_city_pairing = FALSE  
WHERE avoid_same_city_pairing IS NULL;
