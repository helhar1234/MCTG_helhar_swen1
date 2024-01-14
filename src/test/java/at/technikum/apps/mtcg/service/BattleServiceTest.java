package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class BattleServiceTest {

    @Test
    void shouldReturnNoOpponentWhenNoOpponentJoins() {
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic, battlesWaiting, mockedSessionService, mockedDeckService);

        User player = new User("userId", "username", "password");
        when(mockedDeckService.hasDeckSet(player.getId())).thenReturn(true);

        // Assert that an HttpStatusException is thrown with a specific message
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> battleService.battle(player)
        );

        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("No opponent found for battle - Try again later:)", exception.getMessage());
    }


    @Test
    void shouldThrowExceptionWhenNoDeckSet() {
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic, battlesWaiting, mockedSessionService, mockedDeckService);

        User player = new User("userId", "username", "password");
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
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        BattleLogic mockedBattleLogic = mock(BattleLogic.class);
        SessionService mockedSessionService = mock(SessionService.class);
        DeckService mockedDeckService = mock(DeckService.class);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();

        BattleService battleService = new BattleService(mockedBattleRepository, mockedBattleLogic,battlesWaiting, mockedSessionService, mockedDeckService);
        User playerA = new User("playerAId", "playerA", "password");
        User playerB = new User("playerBId", "playerB", "password");
        when(mockedDeckService.hasDeckSet(anyString())).thenReturn(true);

        String battleId = UUID.randomUUID().toString();
        BattleResult openBattle = new BattleResult(battleId, playerA, "waiting");
        battlesWaiting.put(battleId, openBattle);

        // Simulate logic for a completed battle
        BattleResult completedBattle = new BattleResult(battleId, playerA, playerB, "completed", playerA, "startTime", "logEntry");
        when(mockedBattleLogic.performBattle(eq(battleId), any(User.class), any(User.class))).thenReturn(completedBattle);
        when(mockedBattleRepository.findBattleById(battleId)).thenReturn(Optional.of(completedBattle));

        BattleResult battleResult = battleService.battle(playerB);

        assertEquals("completed", battleResult.getStatus());
        assertNotNull(battleResult.getPlayerB());
        assertNotNull(battleResult.getPlayerA());
    }



    }

