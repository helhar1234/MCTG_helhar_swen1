package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class BattleService {
    private final BattleRepository battleRepository;
    private final BattleLogic battleLogic;
    private final ConcurrentHashMap<String, BattleResult> battlesWaiting; // kümmert sich um mutexes selbst
    private final SessionService sessionService;
    private final DeckService deckService;

    public BattleService(BattleRepository battleRepository, BattleLogic battleLogic, ConcurrentHashMap<String, BattleResult> battlesWaiting, SessionService sessionService, DeckService deckService) {
        this.battleRepository = battleRepository;
        this.battleLogic = battleLogic;
        this.battlesWaiting = battlesWaiting;
        this.sessionService = sessionService;
        this.deckService = deckService;
    }

    public BattleResult start(User player) {
        // Find an open battle
        Optional<BattleResult> openBattle = findOpenBattle(player);

        // If there is an open battle, join it
        if (openBattle.isPresent()) {
            BattleResult battle = openBattle.get();
            battle.setPlayerB(player); // Set player as Player B
            battlesWaiting.put(battle.getId(), battle); // Update the waiting battle
            return waitForBattleCompletion(battle.getId());
        } else {
            // Create a new battle if none are open
            String battleId = UUID.randomUUID().toString();
            BattleResult newBattle = new BattleResult(battleId, player, "waiting");
            battlesWaiting.put(battleId, newBattle);

            // Wait for another player to join or timeout
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 2000) { // 20s wait time
                BattleResult updatedBattle = battlesWaiting.get(battleId);
                if (updatedBattle.getPlayerB() != null) {
                    // If another player joined, start the battle
                    return battleLogic.performBattle(battleId, player, updatedBattle.getPlayerB());
                }
            }

            // If no player joins in 1 minute, delete the battle
            battlesWaiting.remove(battleId);
            return new BattleResult(battleId, player, "no_opponent");
        }
    }

    private Optional<BattleResult> findOpenBattle(User player) {
        for (BattleResult battle : battlesWaiting.values()) {
            if (battle.getPlayerB() == null && Objects.equals(battle.getStatus(), "waiting") && battle.getPlayerA() != player) {
                return Optional.of(battle);
            }
        }
        return Optional.empty();
    }

    private BattleResult waitForBattleCompletion(String battleId) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 60000) { // Wait up to 1 minute
            Optional<BattleResult> updatedBattle = null;
            updatedBattle = battleRepository.findBattleById(battleId);
            if (updatedBattle.isPresent() && updatedBattle.get().getStatus().equals("completed")) {
                return updatedBattle.get();
            }
        }
        return null; // Return null if the battle does not complete in time
    }

    public BattleResult battle(User player) {

        if (!deckService.hasDeckSet(player.getId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Player " + player.getUsername() + " has no deck set up");
        }

        BattleResult battleResult = start(player);
        if (battleResult.getStatus().equals("no_opponent")) {
            throw new HttpStatusException(HttpStatus.OK, "No opponent found for battle - Try again later:)");
        }
        return battleResult;
    }
}

