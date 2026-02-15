-- Dane początkowe dla systemu turniejowego
-- Warhammer 40,000 10th Edition

-- Usuń istniejące dane dla Warhammer 40k (jeśli istnieją)
DO $$
DECLARE
    wh40k_id INTEGER;
BEGIN
    SELECT id INTO wh40k_id FROM tourney_schema.gamesystems WHERE name = 'Warhammer 40,000 10th Edition';
    
    IF wh40k_id IS NOT NULL THEN
        DELETE FROM tourney_schema.army_factions WHERE game_system_id = wh40k_id;
        DELETE FROM tourney_schema.primary_missions WHERE game_system_id = wh40k_id;
        DELETE FROM tourney_schema.deployments WHERE game_system_id = wh40k_id;
        DELETE FROM tourney_schema.gamesystems WHERE id = wh40k_id;
    END IF;
END $$;

-- GameSystem
INSERT INTO tourney_schema.gamesystems (name, default_round_number, primary_score_enabled, secondary_score_enabled, third_score_enabled, additional_score_enabled)
VALUES ('Warhammer 40,000 10th Edition', 5, true, true, false, false);

-- Pobierz ID utworzonego systemu
DO $$
DECLARE
    wh40k_id INTEGER;
BEGIN
    SELECT id INTO wh40k_id FROM tourney_schema.gamesystems WHERE name = 'Warhammer 40,000 10th Edition';

    -- Deployments
    INSERT INTO tourney_schema.deployments (game_system_id, name) VALUES
    (wh40k_id, 'Tripping Point'),
    (wh40k_id, 'Sweeping Engagement'),
    (wh40k_id, 'Search and Destroy'),
    (wh40k_id, 'Hammer and Anvil'),
    (wh40k_id, 'Dawn of War'),
    (wh40k_id, 'Crucible of Battle');

    -- Primary Missions
    INSERT INTO tourney_schema.primary_missions (game_system_id, name) VALUES
    (wh40k_id, 'Linchpin'),
    (wh40k_id, 'Burden of Trust'),
    (wh40k_id, 'Take and Hold'),
    (wh40k_id, 'Terraform'),
    (wh40k_id, 'Purge the Foe'),
    (wh40k_id, 'Scorched Earth'),
    (wh40k_id, 'Unexploded Ordnance'),
    (wh40k_id, 'Hidden Supplies'),
    (wh40k_id, 'The Ritual'),
    (wh40k_id, 'Supply Drop');

    -- Army Factions
    INSERT INTO tourney_schema.army_factions (game_system_id, name) VALUES
    (wh40k_id, 'Imperium'),
    (wh40k_id, 'Xenos'),
    (wh40k_id, 'Chaos');
    
END $$;

-- Armies powiązane z fakcjami
DO $$
DECLARE
    imperium_id INTEGER;
    xenos_id INTEGER;
    chaos_id INTEGER;
BEGIN
    SELECT id INTO imperium_id FROM tourney_schema.army_factions WHERE name = 'Imperium';
    SELECT id INTO xenos_id FROM tourney_schema.army_factions WHERE name = 'Xenos';
    SELECT id INTO chaos_id FROM tourney_schema.army_factions WHERE name = 'Chaos';

    -- Imperium Armies
    INSERT INTO tourney_schema.armies (name, army_faction_id) VALUES
    ('Space Marines', imperium_id),
    ('Black Templars', imperium_id),
    ('Blood Angels', imperium_id),
    ('Dark Angels', imperium_id),
    ('Deathwatch', imperium_id),
    ('Grey Knights', imperium_id),
    ('Space Wolves', imperium_id),
    ('Adepta Sororitas', imperium_id),
    ('Adeptus Custodes', imperium_id),
    ('Adeptus Mechanicus', imperium_id),
    ('Astra Militarum', imperium_id),
    ('Imperial Knights', imperium_id),
    ('Imperial Agents', imperium_id);

    -- Xenos Armies
    INSERT INTO tourney_schema.armies (name, army_faction_id) VALUES
    ('Aeldari', xenos_id),
    ('Drukhari', xenos_id),
    ('Tyranids', xenos_id),
    ('Genestealer Cults', xenos_id),
    ('Leagues of Votann', xenos_id),
    ('Necrons', xenos_id),
    ('Orks', xenos_id),
    ('T''au Empire', xenos_id);

    -- Chaos Armies
    INSERT INTO tourney_schema.armies (name, army_faction_id) VALUES
    ('Chaos Space Marines', chaos_id),
    ('Death Guard', chaos_id),
    ('Thousand Sons', chaos_id),
    ('World Eaters', chaos_id),
    ('Chaos Daemons', chaos_id),
    ('Chaos Knights', chaos_id),
    ('Emperor''s Children', chaos_id);
    
END $$;
