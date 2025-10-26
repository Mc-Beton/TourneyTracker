package com.tourney.domain.scores;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tie_breakers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TieBreaker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TieBreakerType type;

    private Double value;

    private Integer priority;

    public enum TieBreakerType {
        MATCH_WINS("Liczba wygranych meczy", "Suma wszystkich wygranych meczy"),
        MATCH_WIN_PERCENTAGE("Procent wygranych", "Procent wygranych meczy z wszystkich rozegranych"),
        GAME_WIN_PERCENTAGE("Procent wygranych gier", "Procent wygranych pojedynczych gier"),
        OPPONENTS_MATCH_WIN_PERCENTAGE("Procent wygranych przeciwników", "Średni procent wygranych meczy przeciwników"),
        OPPONENTS_GAME_WIN_PERCENTAGE("Procent wygranych gier przeciwników", "Średni procent wygranych gier przeciwników"),
        HEAD_TO_HEAD("Bezpośrednie mecze", "Wynik bezpośrednich pojedynków"),
        TOTAL_SCORE("Suma punktów", "Całkowita suma zdobytych punktów");

        private final String displayName;
        private final String description;

        TieBreakerType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f (priorytet: %d)", 
                type.getDisplayName(), 
                value, 
                priority);
    }
}