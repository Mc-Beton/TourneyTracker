-- Dodanie kolumny pairing_algorithm do tabeli tournament_round_definitions
ALTER TABLE tourney_schema.tournament_round_definitions
ADD COLUMN IF NOT EXISTS pairing_algorithm VARCHAR(255);

-- Ustawienie wartości domyślnej dla wszystkich istniejących rekordów
UPDATE tourney_schema.tournament_round_definitions
SET pairing_algorithm = 'STANDARD'
WHERE pairing_algorithm IS NULL;

-- Dodanie constraint NOT NULL
ALTER TABLE tourney_schema.tournament_round_definitions
ALTER COLUMN pairing_algorithm SET NOT NULL;

-- Dodanie check constraint
ALTER TABLE tourney_schema.tournament_round_definitions
ADD CONSTRAINT check_pairing_algorithm CHECK (pairing_algorithm IN ('STANDARD', 'CUSTOM'));
