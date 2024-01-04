package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.*;

import java.util.*;
import java.util.concurrent.*;

public class BattleService {
    private final BattleRepository battleRepository;
    private final BattleLogic battleLogic;
    private final ConcurrentHashMap<String, BattleResult> battlesWaiting;

    public BattleService() {
        this.battleRepository = new BattleRepository_db();
        this.battleLogic = new BattleLogic();
        this.battlesWaiting = new ConcurrentHashMap<>();
    }

    public BattleResult battle(User player) {
        // Find an open battle
        Optional<BattleResult> openBattle = findOpenBattle();

        // If there is an open battle, join it
        if (openBattle.isPresent()) {
            BattleResult battle = openBattle.get();
            return battleLogic.performBattle(battle.getId(), battle.getPlayerA(), player);
        } else {
            // Create a new battle if none are open
            String battleId = UUID.randomUUID().toString();
            BattleResult newBattle = new BattleResult(battleId, player, "waiting");
            battlesWaiting.put(battleId, newBattle);

            // Wait for another player to join or timeout
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 60000) { // 1 minute wait time
                BattleResult updatedBattle = battlesWaiting.get(battleId);
                if (updatedBattle != null && updatedBattle.getPlayerB() != null) {
                    // If another player joined, start the battle
                    return battleLogic.performBattle(battleId, player, updatedBattle.getPlayerB());
                }
            }

            // If no player joins in 1 minute, delete the battle
            battlesWaiting.remove(battleId);
            return null; // Indicate that the battle was cancelled
        }
    }

    private Optional<BattleResult> findOpenBattle() {
        for (BattleResult battle : battlesWaiting.values()) {
            if (battle.getPlayerB() == null && Objects.equals(battle.getStatus(), "waiting")) {
                return Optional.of(battle);
            }
        }
        return Optional.empty();
    }
}

