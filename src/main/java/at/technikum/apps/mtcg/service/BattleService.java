package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Initiates a battle for a player, either by joining an open battle or creating a new one.
     *
     * @param player The player initiating the battle.
     * @return The result of the battle, which could be waiting, in-progress, or a no-opponent scenario.
     */
    public BattleResult start(User player) {
        // Search for an open battle that the player can join
        Optional<BattleResult> openBattle = findOpenBattle(player);

        // If there's an open battle, join it
        if (openBattle.isPresent()) {
            BattleResult battle = openBattle.get();
            battle.setPlayerB(player); // Set the current player as Player B
            battlesWaiting.put(battle.getId(), battle); // Update the battle's status in the waiting pool
            // Wait for the battle to complete and return the result
            return waitForBattleCompletion(battle.getId());
        } else {
            // If no open battle is available, create a new battle
            String battleId = UUID.randomUUID().toString();
            BattleResult newBattle = new BattleResult(battleId, player, "waiting");
            battlesWaiting.put(battleId, newBattle); // Add the new battle to the waiting pool

            // Wait for a fixed time for another player to join
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 20000) { // 20 seconds timeout
                BattleResult updatedBattle = battlesWaiting.get(battleId);
                // If another player joins, start the battle
                if (updatedBattle != null && updatedBattle.getPlayerB() != null) {
                    return battleLogic.performBattle(battleId, player, updatedBattle.getPlayerB());
                }
            }

            // If no player joins within the timeout, remove the battle from waiting and return a no-opponent result
            battlesWaiting.remove(battleId);
            return new BattleResult(battleId, player, "no_opponent");
        }
    }


    /**
     * Searches for an open battle that the player can join.
     *
     * @param player The player looking to join a battle.
     * @return An Optional containing an open BattleResult if one is found, or an empty Optional if not.
     */
    private Optional<BattleResult> findOpenBattle(User player) {
        // Iterate over the current battles waiting for an opponent
        for (BattleResult battle : battlesWaiting.values()) {
            // Check if the battle is open and the player is not already part of it
            if (battle.getPlayerB() == null && "waiting".equals(battle.getStatus()) && !battle.getPlayerA().equals(player)) {
                return Optional.of(battle);
            }
        }
        return Optional.empty();
    }


    /**
     * Waits for a battle to complete and returns the result.
     *
     * @param battleId The ID of the battle to wait for.
     * @return The completed BattleResult, or null if the battle does not complete within the timeout.
     */
    private BattleResult waitForBattleCompletion(String battleId) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 60000) { // 1-minute timeout
            Optional<BattleResult> updatedBattle = battleRepository.findBattleById(battleId);
            // Check if the battle has completed
            if (updatedBattle.isPresent() && "completed".equals(updatedBattle.get().getStatus())) {
                return updatedBattle.get();
            }
        }
        // Return null if the battle does not complete within the timeout
        return null;
    }


    /**
     * Facilitates a battle for the player.
     *
     * @param player The player participating in the battle.
     * @return The result of the battle.
     * @throws HttpStatusException If the player has not set up a deck or if no opponent is found for the battle.
     */
    public BattleResult battle(User player) {
        // Check if the player has a deck set up
        if (!deckService.hasDeckSet(player.getId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Player " + player.getUsername() + " has no deck set up");
        }

        // Start the battle process
        BattleResult battleResult = start(player);
        // Handle the scenario where no opponent is found
        if ("no_opponent".equals(battleResult.getStatus())) {
            throw new HttpStatusException(HttpStatus.OK,
                    "Player " + player.getUsername() + " has no opponent found for battle - Try again later:)");
        }
        return battleResult;
    }
}

