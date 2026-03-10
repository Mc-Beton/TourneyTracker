-- Migration: Replace CUSTOM with BACKTRACKING and add CP_SAT option
-- This migration removes the CUSTOM pairing algorithm type and replaces it with BACKTRACKING
-- Also adds the new CP_SAT algorithm option

-- Step 1: Drop the existing check constraint (trying both possible names)
ALTER TABLE tourney_schema.tournament_round_definitions
DROP CONSTRAINT IF EXISTS check_pairing_algorithm;

ALTER TABLE tourney_schema.tournament_round_definitions
DROP CONSTRAINT IF EXISTS tournament_round_definitions_pairing_algorithm_check;

-- Step 2: Update all CUSTOM values to BACKTRACKING
UPDATE tourney_schema.tournament_round_definitions
SET pairing_algorithm = 'BACKTRACKING'
WHERE pairing_algorithm = 'CUSTOM';

-- Step 3: Add new check constraint with updated values (STANDARD, BACKTRACKING, CP_SAT)
ALTER TABLE tourney_schema.tournament_round_definitions
ADD CONSTRAINT tournament_round_definitions_pairing_algorithm_check 
CHECK (pairing_algorithm IN ('STANDARD', 'BACKTRACKING', 'CP_SAT'));
