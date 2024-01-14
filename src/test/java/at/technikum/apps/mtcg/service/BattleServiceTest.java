package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BattleServiceTest {

    @Test
    void shouldReturnNoOpponentWhenNoOpponentJoins() {
        // Mock dependencies and create an instance of BattleService
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic, battlesWaiting, mockedSessionService, mockedDeckService);

        // Create a user instance
        User player = new User("userId", "username", "password");

        // Configure the mock behavior for checking if a deck is set for the player
        when(mockedDeckService.hasDeckSet(player.getId())).thenReturn(true);

        // Assert that an HttpStatusException is thrown with a specific message
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> battleService.battle(player)
        );

        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("Player " + player.getUsername() + " has no opponent found for battle - Try again later:)", exception.getMessage());
    }


    @Test
    void shouldThrowExceptionWhenNoDeckSet() {
        // Mock dependencies and create an instance of BattleService
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic, battlesWaiting, mockedSessionService, mockedDeckService);

        // Create a user instance
        User player = new User("userId", "username", "password");

        // Configure the mock behavior for checking if a deck is set for the player
        when(mockedDeckService.hasDeckSet(player.getId())).thenReturn(false);

        // Assert that an HttpStatusException is thrown
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> battleService.battle(player)
        );

        // Additional assertions can be added here if needed, such as checking the exception's message
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Player username has no deck set up", exception.getMessage());
    }


    @Test
    void shouldCompleteBattle() {
        // Mock dependencies and create an instance of BattleService
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic, battlesWaiting, mockedSessionService, mockedDeckService);

        // Create user instances for players A and B
        User playerA = new User("playerAId", "playerA", "password");
        User playerB = new User("playerBId", "playerB", "password");

        // Configure the mock behavior for checking if a deck is set for any user
        when(mockedDeckService.hasDeckSet(anyString())).thenReturn(true);

        // Generate a unique battle ID
        String battleId = UUID.randomUUID().toString();

        // Create an open battle result and add it to the battlesWaiting map
        BattleResult openBattle = new BattleResult(battleId, playerA, "waiting");
        battlesWaiting.put(battleId, openBattle);

        // Simulate logic for a completed battle
        BattleResult completedBattle = new BattleResult(battleId, playerA, playerB, "completed", playerA, "startTime", "logEntry");
        when(mockedBattleLogic.performBattle(eq(battleId), any(User.class), any(User.class))).thenReturn(completedBattle);
        when(mockedBattleRepository.findBattleById(battleId)).thenReturn(Optional.of(completedBattle));

        // Perform the battle
        BattleResult battleResult = battleService.battle(playerB);

        // Assertions to check the completed battle result
        assertEquals("completed", battleResult.getStatus());
        assertNotNull(battleResult.getPlayerB());
        assertNotNull(battleResult.getPlayerA());
    }


}

