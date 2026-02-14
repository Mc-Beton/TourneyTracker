-- Dodanie kolumny player_level_pairing_strategy do tabeli tournament_round_definitions
ALTER TABLE tourney_schema.tournament_round_definitions
ADD COLUMN IF NOT EXISTS player_level_pairing_strategy VARCHAR(255);

-- Ustawienie wartości domyślnej dla wszystkich istniejących rekordów
UPDATE tourney_schema.tournament_round_definitions
SET player_level_pairing_strategy = 'NONE'
WHERE player_level_pairing_strategy IS NULL;

-- Dodanie constraint NOT NULL
ALTER TABLE tourney_schema.tournament_round_definitions
ALTER COLUMN player_level_pairing_strategy SET NOT NULL;

-- Dodanie check constraint
ALTER TABLE tourney_schema.tournament_round_definitions
ADD CONSTRAINT check_player_level_pairing_strategy 
CHECK (player_level_pairing_strategy IN ('NONE', 'BEGINNERS_WITH_VETERANS', 'BEGINNERS_WITH_BEGINNERS'));
