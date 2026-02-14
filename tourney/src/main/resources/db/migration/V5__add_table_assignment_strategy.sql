-- Dodanie kolumny table_assignment_strategy do tabeli tournament_round_definitions
ALTER TABLE tourney_schema.tournament_round_definitions
ADD COLUMN IF NOT EXISTS table_assignment_strategy VARCHAR(255);

-- Ustawienie wartości domyślnej dla wszystkich istniejących rekordów
UPDATE tourney_schema.tournament_round_definitions
SET table_assignment_strategy = 'BEST_FIRST'
WHERE table_assignment_strategy IS NULL;

-- Dodanie constraint NOT NULL
ALTER TABLE tourney_schema.tournament_round_definitions
ALTER COLUMN table_assignment_strategy SET NOT NULL;

-- Dodanie check constraint
ALTER TABLE tourney_schema.tournament_round_definitions
ADD CONSTRAINT check_table_assignment_strategy 
CHECK (table_assignment_strategy IN ('BEST_FIRST', 'RANDOM'));
