package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.BattleRepository;
import at.technikum.apps.mtcg.repository.BattleRepository_db;
import at.technikum.apps.mtcg.repository.CardRepository_db;
import at.technikum.apps.mtcg.repository.UserRepository_db;

import java.util.Random;

public class BattleLogic {
    private final BattleRepository battleRepository;
    public BattleLogic() {
        this.battleRepository = new BattleRepository_db();

    }

    // Diese Methode sollte die Logik enthalten, um eine Schlacht zwischen zwei Spielern durchzuführen.
    public BattleResult performBattle(String battleId, User playerA, User playerB) {
        boolean started = battleRepository.startBattle(battleId, playerA.getId(),playerB.getId());
        // Spiellogik hier implementieren. Zum Beispiel:
        // - Karten der Spieler vergleichen
        // - Berechnen, wer gewinnt
        // - Ein BattleResult-Objekt mit den Ergebnissen zurückgeben

        // Dummy-Logik, die zufällig einen Gewinner auswählt
        Random rand = new Random();
        User winner = rand.nextBoolean() ? playerA : playerB;
        String battleLog = "The battle was fierce, but ultimately " + winner.getUsername() + " emerged victorious!";

        // Hier sollten die echten Ergebnisse der Schlacht aufgezeichnet werden
        return battleRepository.findBattleById(battleId).get();
    }
}
