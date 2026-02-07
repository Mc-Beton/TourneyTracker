package com.tourney.migration;

import com.tourney.repository.games.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Jednorazowa migracja danych dla wprowadzenia dziedziczenia Match -> TournamentMatch/SingleMatch
 * 
 * MIGRATION COMPLETED - This class can be removed now.
 * All existing matches have been migrated:
 * - TOURNAMENT for matches with round_id
 * - SINGLE for matches without round_id
 */
// @Component - DISABLED AFTER SUCCESSFUL MIGRATION
@RequiredArgsConstructor
@Slf4j
public class MatchTypeDataMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            log.info("🔄 Sprawdzanie czy istnieje kolumna match_type...");
            
            // Sprawdź czy kolumna istnieje
            Integer columnExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = 'tourney_schema' AND table_name = 'matches' AND column_name = 'match_type'",
                    Integer.class
            );
            
            if (columnExists == null || columnExists == 0) {
                log.info("🔧 Kolumna match_type nie istnieje. Tworzę ją...");
                jdbcTemplate.execute("ALTER TABLE tourney_schema.matches ADD COLUMN match_type VARCHAR(31)");
                log.info("✅ Utworzono kolumnę match_type jako nullable");
            } else {
                log.info("✅ Kolumna match_type już istnieje");
                
                // Sprawdź czy jest nullable
                String nullable = jdbcTemplate.queryForObject(
                        "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_schema = 'tourney_schema' AND table_name = 'matches' AND column_name = 'match_type'",
                        String.class
                );
                
                if ("NO".equals(nullable)) {
                    log.info("🔧 Kolumna match_type jest NOT NULL, zmieniam na nullable...");
                    jdbcTemplate.execute("ALTER TABLE tourney_schema.matches ALTER COLUMN match_type DROP NOT NULL");
                    log.info("✅ Zmieniono match_type na nullable");
                }
            }
            
            // Teraz uzupełnij dane
            Integer nullCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tourney_schema.matches WHERE match_type IS NULL",
                    Integer.class
            );
            
            if (nullCount != null && nullCount > 0) {
                log.info("🔧 Znaleziono {} meczów bez match_type. Rozpoczynam migrację...", nullCount);
                
                // Ustaw TOURNAMENT dla meczów turniejowych
                int tournamentUpdated = jdbcTemplate.update(
                        "UPDATE tourney_schema.matches SET match_type = 'TOURNAMENT' WHERE round_id IS NOT NULL AND match_type IS NULL"
                );
                log.info("✅ Zaktualizowano {} meczów turniejowych (match_type = TOURNAMENT)", tournamentUpdated);
                
                // Ustaw SINGLE dla pojedynczych meczów
                int singleUpdated = jdbcTemplate.update(
                        "UPDATE tourney_schema.matches SET match_type = 'SINGLE' WHERE round_id IS NULL AND match_type IS NULL"
                );
                log.info("✅ Zaktualizowano {} pojedynczych meczów (match_type = SINGLE)", singleUpdated);
                
                log.info("🎉 Migracja match_type zakończona pomyślnie! Zaktualizowano łącznie {} rekordów.", 
                        tournamentUpdated + singleUpdated);
            } else {
                log.info("✅ Wszystkie mecze mają już ustawiony match_type. Migracja nie jest potrzebna.");
            }
            
        } catch (Exception e) {
            log.error("❌ Błąd podczas migracji match_type: {}", e.getMessage(), e);
            // Nie przerywamy startu aplikacji - być może pole match_type nie istnieje jeszcze
            // (pierwsze uruchomienie z nowym kodem)
        }
    }
}
