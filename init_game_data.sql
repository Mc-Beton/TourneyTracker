-- Dane początkowe dla systemu turniejowego
-- Warhammer 40,000 10th Edition

-- Zabezpieczenie legacy nazwy systemu:
-- - jeśli istnieje tylko 'Warhammer 40,000' -> zmień nazwę na 'Warhammer 40,000 10th Edition'
-- - jeśli istnieją oba wpisy -> przepnij referencje i usuń legacy rekord
DO $$
DECLARE
    legacy_id INTEGER;
    canonical_id INTEGER;
BEGIN
    SELECT id INTO legacy_id FROM tourney_schema.gamesystems WHERE name = 'Warhammer 40,000';
    SELECT id INTO canonical_id FROM tourney_schema.gamesystems WHERE name = 'Warhammer 40,000 10th Edition';

    IF legacy_id IS NOT NULL THEN
        IF canonical_id IS NULL THEN
            UPDATE tourney_schema.gamesystems
            SET name = 'Warhammer 40,000 10th Edition'
            WHERE id = legacy_id;
        ELSE
            -- Przepnij referencje z legacy systemu
            UPDATE tourney_schema.tournaments
            SET game_system_id = canonical_id
            WHERE game_system_id = legacy_id;

            UPDATE tourney_schema.match_details
            SET game_system_id = canonical_id
            WHERE game_system_id = legacy_id;

            UPDATE tourney_schema.match_details
            SET primary_mission_id = NULL
            WHERE primary_mission_id IN (
                SELECT pm.id
                FROM tourney_schema.primary_missions pm
                WHERE pm.game_system_id = legacy_id
            );

            UPDATE tourney_schema.match_details
            SET deployment_id = NULL
            WHERE deployment_id IN (
                SELECT d.id
                FROM tourney_schema.deployments d
                WHERE d.game_system_id = legacy_id
            );

            UPDATE tourney_schema.tournament_round_definitions
            SET primary_mission_id = NULL
            WHERE primary_mission_id IN (
                SELECT pm.id
                FROM tourney_schema.primary_missions pm
                WHERE pm.game_system_id = legacy_id
            );

            UPDATE tourney_schema.tournament_round_definitions
            SET deployment_id = NULL
            WHERE deployment_id IN (
                SELECT d.id
                FROM tourney_schema.deployments d
                WHERE d.game_system_id = legacy_id
            );

            UPDATE tourney_schema.tournament_participants
            SET army_id = NULL
            WHERE army_id IN (
                SELECT a.id
                FROM tourney_schema.armies a
                JOIN tourney_schema.army_factions af ON af.id = a.army_faction_id
                WHERE af.game_system_id = legacy_id
            );

            UPDATE tourney_schema.tournament_participants
            SET army_faction_id = NULL
            WHERE army_faction_id IN (
                SELECT af.id
                FROM tourney_schema.army_factions af
                WHERE af.game_system_id = legacy_id
            );

            DELETE FROM tourney_schema.armies a
            USING tourney_schema.army_factions af
            WHERE a.army_faction_id = af.id
              AND af.game_system_id = legacy_id;

            DELETE FROM tourney_schema.primary_missions WHERE game_system_id = legacy_id;
            DELETE FROM tourney_schema.deployments WHERE game_system_id = legacy_id;
            DELETE FROM tourney_schema.army_factions WHERE game_system_id = legacy_id;
            DELETE FROM tourney_schema.gamesystems WHERE id = legacy_id;
        END IF;
    END IF;
END $$;

-- Zabezpieczenie literówki nazwy Eldfall:
-- - jeśli istnieje tylko 'Eldafll Chronicles' -> zmień nazwę na 'Eldfall Chronicles'
-- - jeśli istnieją oba wpisy -> usuń literówkę po przepięciu referencji game_system_id
DO $$
DECLARE
    typo_id INTEGER;
    canonical_id INTEGER;
BEGIN
    SELECT id INTO typo_id FROM tourney_schema.gamesystems WHERE name = 'Eldafll Chronicles';
    SELECT id INTO canonical_id FROM tourney_schema.gamesystems WHERE name = 'Eldfall Chronicles';

    IF typo_id IS NOT NULL THEN
        IF canonical_id IS NULL THEN
            UPDATE tourney_schema.gamesystems
            SET name = 'Eldfall Chronicles'
            WHERE id = typo_id;
        ELSE
            UPDATE tourney_schema.tournaments
            SET game_system_id = canonical_id
            WHERE game_system_id = typo_id;

            UPDATE tourney_schema.match_details
            SET game_system_id = canonical_id
            WHERE game_system_id = typo_id;

            UPDATE tourney_schema.deployments
            SET game_system_id = canonical_id
            WHERE game_system_id = typo_id;

            UPDATE tourney_schema.primary_missions
            SET game_system_id = canonical_id
            WHERE game_system_id = typo_id;

            UPDATE tourney_schema.army_factions
            SET game_system_id = canonical_id
            WHERE game_system_id = typo_id;

            DELETE FROM tourney_schema.gamesystems
            WHERE id = typo_id;
        END IF;
    END IF;
END $$;

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

-- GameSystems
INSERT INTO tourney_schema.gamesystems (
    name,
    default_round_number,
    primary_score_enabled,
    secondary_score_enabled,
    third_score_enabled,
    additional_score_enabled
)
SELECT
    game_data.name,
    game_data.default_round_number,
    game_data.primary_score_enabled,
    game_data.secondary_score_enabled,
    game_data.third_score_enabled,
    game_data.additional_score_enabled
FROM (
    VALUES
        ('Warhammer 40,000 10th Edition', 5, true, true, false, false),
    ('Eldfall Chronicles', 5, true, false, false, false),
        ('Bolt Action', 5, true, false, false, false)
) AS game_data(name, default_round_number, primary_score_enabled, secondary_score_enabled, third_score_enabled, additional_score_enabled)
WHERE NOT EXISTS (
    SELECT 1
    FROM tourney_schema.gamesystems gs
    WHERE gs.name = game_data.name
);

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

-- Eldfall Chronicles - Army Factions (bez armii)
DO $$
DECLARE
    eldfall_id INTEGER;
BEGIN
    SELECT id INTO eldfall_id FROM tourney_schema.gamesystems WHERE name = 'Eldfall Chronicles';

    IF eldfall_id IS NOT NULL THEN
        INSERT INTO tourney_schema.army_factions (game_system_id, name)
        SELECT
            eldfall_id,
            faction_data.name
        FROM (
            VALUES
                ('Empire of Soga'),
                ('Helian League'),
                ('Coalition of Thenion'),
                ('Sand Kingdoms'),
                ('Oni Clans'),
                ('Goblin Wartribes')
        ) AS faction_data(name)
        WHERE NOT EXISTS (
            SELECT 1
            FROM tourney_schema.army_factions af
            WHERE af.game_system_id = eldfall_id
              AND af.name = faction_data.name
        );
    END IF;
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
