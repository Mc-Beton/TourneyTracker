-- Skrypt czyszczący wszystkie dane słownikowe
-- UWAGA: Usuwa wszystkie systemy gier, misje, rozmieszczenia, frakcje i armie

-- Usuń armies (powiązane z frakcjami)
DELETE FROM tourney_schema.armies;

-- Usuń army_factions (powiązane z game_system)
DELETE FROM tourney_schema.army_factions;

-- Usuń primary_missions (powiązane z game_system)
DELETE FROM tourney_schema.primary_missions;

-- Usuń deployments (powiązane z game_system)
DELETE FROM tourney_schema.deployments;

-- Usuń gamesystems
DELETE FROM tourney_schema.gamesystems;

-- Zresetuj sekwencje ID
ALTER SEQUENCE tourney_schema.armies_id_seq RESTART WITH 1;
ALTER SEQUENCE tourney_schema.army_factions_id_seq RESTART WITH 1;
ALTER SEQUENCE tourney_schema.primary_missions_id_seq RESTART WITH 1;
ALTER SEQUENCE tourney_schema.deployments_id_seq RESTART WITH 1;
ALTER SEQUENCE tourney_schema.gamesystems_id_seq RESTART WITH 1;
