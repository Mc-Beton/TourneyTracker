-- Add payment_required column to leagues table
ALTER TABLE tourney_schema.leagues 
ADD COLUMN payment_required BOOLEAN NOT NULL DEFAULT false;

-- Add has_paid column to league_members table
ALTER TABLE tourney_schema.league_members 
ADD COLUMN has_paid BOOLEAN NOT NULL DEFAULT false;

-- Add match_mode column to league_challenges table
ALTER TABLE tourney_schema.league_challenges 
ADD COLUMN match_mode VARCHAR(20) NOT NULL DEFAULT 'LIVE';
