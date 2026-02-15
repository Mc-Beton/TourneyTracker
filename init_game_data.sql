-- Dane początkowe dla systemu turniejowego
-- Warhammer 40,000 10th Edition

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
    (wh40k_id, 'Dawn of War'),
    (wh40k_id, 'Hammer and Anvil'),
    (wh40k_id, 'Search and Destroy'),
    (wh40k_id, 'Crucible of Battle'),
    (wh40k_id, 'Sweeping Engagement');

    -- Primary Missions
    INSERT INTO tourney_schema.primary_missions (game_system_id, name) VALUES
    (wh40k_id, 'Take and Hold'),
    (wh40k_id, 'Purge the Foe'),
    (wh40k_id, 'Sites of Power'),
    (wh40k_id, 'Supply Drop'),
    (wh40k_id, 'Deploy Servo-skulls'),
    (wh40k_id, 'The Ritual');

    -- Army Factions
    INSERT INTO tourney_schema.army_factions (game_system_id, name) VALUES
    (wh40k_id, 'Space Marines'),
    (wh40k_id, 'Chaos Space Marines'),
    (wh40k_id, 'Astra Militarum'),
    (wh40k_id, 'Orks'),
    (wh40k_id, 'Necrons'),
    (wh40k_id, 'Tyranids'),
    (wh40k_id, 'Eldar'),
    (wh40k_id, 'Dark Eldar'),
    (wh40k_id, 'T''au Empire'),
    (wh40k_id, 'Adeptus Mechanicus'),
    (wh40k_id, 'Chaos Daemons'),
    (wh40k_id, 'Death Guard'),
    (wh40k_id, 'Thousand Sons'),
    (wh40k_id, 'Grey Knights'),
    (wh40k_id, 'Adeptus Custodes'),
    (wh40k_id, 'Imperial Knights'),
    (wh40k_id, 'Chaos Knights'),
    (wh40k_id, 'Leagues of Votann'),
    (wh40k_id, 'Genestealer Cults'),
    (wh40k_id, 'Adepta Sororitas'),
    (wh40k_id, 'World Eaters');

    -- Armies (powiązane z fakcjami)
    INSERT INTO tourney_schema.armies (name) VALUES
    ('Custom Army List');
    
END $$;

COMMIT;
