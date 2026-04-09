-- Change tournaments.description to TEXT to allow long, multi-paragraph descriptions
ALTER TABLE tournaments
    ALTER COLUMN description TYPE TEXT;