package com.tourney.migration;

import com.tourney.domain.systems.Army;
import com.tourney.domain.systems.ArmyFaction;
import com.tourney.domain.systems.GameSystem;
import com.tourney.repository.systems.ArmyFactionRepository;
import com.tourney.repository.systems.ArmyRepository;
import com.tourney.repository.systems.GameSystemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inicjalizator danych dla Warhammer 40,000
 * Tworzy GameSystem, frakcje i armie
 * WYŁĄCZONY - dane są ładowane z init_game_data.sql
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class Warhammer40kDataInitializer implements ApplicationRunner {

    private static final String LEGACY_NAME = "Warhammer 40,000";
    private static final String CANONICAL_NAME = "Warhammer 40,000 10th Edition";

    private final GameSystemRepository gameSystemRepository;
    private final ArmyFactionRepository armyFactionRepository;
    private final ArmyRepository armyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            log.info("🎮 Inicjalizacja danych Warhammer 40,000...");

            // Sprawdź czy GameSystem istnieje (najpierw nazwa docelowa, potem legacy)
            List<GameSystem> systems = gameSystemRepository.findAll();

            GameSystem canonical = systems.stream()
                    .filter(gs -> CANONICAL_NAME.equals(gs.getName()))
                    .findFirst()
                    .orElse(null);

            GameSystem legacy = systems.stream()
                    .filter(gs -> LEGACY_NAME.equals(gs.getName()))
                    .findFirst()
                    .orElse(null);

            GameSystem wh40k;
            if (canonical != null) {
                wh40k = canonical;
            } else if (legacy != null) {
                log.info("🛠️  Migruję nazwę GameSystem z '{}' na '{}'", LEGACY_NAME, CANONICAL_NAME);
                legacy.setName(CANONICAL_NAME);
                wh40k = gameSystemRepository.save(legacy);
            } else {
                log.info("📦 Tworzę GameSystem: {}", CANONICAL_NAME);
                GameSystem newGs = new GameSystem();
                newGs.setName(CANONICAL_NAME);
                newGs.setDefaultRoundNumber(5);
                newGs.setPrimaryScoreEnabled(true);
                newGs.setSecondaryScoreEnabled(true);
                newGs.setThirdScoreEnabled(false);
                newGs.setAdditionalScoreEnabled(false);
                wh40k = gameSystemRepository.save(newGs);
            }

            // Frakcja Imperium
            ArmyFaction imperium = createOrGetFaction(wh40k, "Imperium");
            createArmiesForFaction(imperium, List.of(
                    "Space Marines",
                    "Black Templars",
                    "Blood Angels",
                    "Dark Angels",
                    "Deathwatch",
                    "Grey Knights",
                    "Space Wolves",
                    "Adepta Sororitas",
                    "Adeptus Custodes",
                    "Adeptus Mechanicus",
                    "Astra Militarum",
                    "Imperial Knights",
                    "Imperial Agents"
            ));

            // Frakcja Xenos
            ArmyFaction xenos = createOrGetFaction(wh40k, "Xenos");
            createArmiesForFaction(xenos, List.of(
                    "Aeldari",
                    "Drukhari",
                    "Tyranids",
                    "Genestealer Cults",
                    "Leagues of Votann",
                    "Necrons",
                    "Orks",
                    "T'au Empire"
            ));

            // Frakcja Chaos
            ArmyFaction chaos = createOrGetFaction(wh40k, "Chaos");
            createArmiesForFaction(chaos, List.of(
                    "Chaos Space Marines",
                    "Death Guard",
                    "Thousand Sons",
                    "World Eaters",
                    "Chaos Daemons",
                    "Chaos Knights",
                    "Emperor's Children"
            ));

            log.info("✅ Inicjalizacja danych Warhammer 40,000 zakończona pomyślnie!");

        } catch (Exception e) {
            log.error("❌ Błąd podczas inicjalizacji danych Warhammer 40,000: {}", e.getMessage(), e);
        }
    }

    private ArmyFaction createOrGetFaction(GameSystem gameSystem, String factionName) {
        return armyFactionRepository.findByGameSystemIdOrderByNameAsc(gameSystem.getId()).stream()
                .filter(f -> f.getName().equals(factionName))
                .findFirst()
                .orElseGet(() -> {
                    log.info("🏴 Tworzę frakcję: {}", factionName);
                    ArmyFaction faction = new ArmyFaction();
                    faction.setName(factionName);
                    faction.setGameSystem(gameSystem);
                    return armyFactionRepository.save(faction);
                });
    }

    private void createArmiesForFaction(ArmyFaction faction, List<String> armyNames) {
        List<Army> existingArmies = armyRepository.findByArmyFactionIdOrderByNameAsc(faction.getId());
        
        for (String armyName : armyNames) {
            boolean exists = existingArmies.stream()
                    .anyMatch(a -> a.getName().equals(armyName));
            
            if (!exists) {
                log.info("⚔️  Tworzę armię: {} (frakcja: {})", armyName, faction.getName());
                Army army = new Army();
                army.setName(armyName);
                army.setArmyFaction(faction);
                armyRepository.save(army);
            }
        }
    }
}
