-- Convert match time columns to timestamptz, interpreting existing values as UTC
-- This ensures DB stores an absolute instant; API will return values with explicit offset

DO $$
BEGIN
    -- Ensure schema exists (no-op if already present)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.schemata WHERE schema_name = 'tourney_schema'
    ) THEN
        RAISE NOTICE 'Schema tourney_schema does not exist; skipping';
    END IF;
END $$;

-- Convert columns on tourney_schema.matches
ALTER TABLE IF EXISTS tourney_schema.matches
    ALTER COLUMN start_time TYPE timestamptz USING (start_time AT TIME ZONE 'UTC'),
    ALTER COLUMN result_submission_deadline TYPE timestamptz USING (result_submission_deadline AT TIME ZONE 'UTC'),
    ALTER COLUMN game_end_time TYPE timestamptz USING (game_end_time AT TIME ZONE 'UTC');
