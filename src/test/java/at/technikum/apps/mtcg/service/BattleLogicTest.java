package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.elo.ELORepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BattleLogicTest {

    @Test
    void testDrawCondition() {
        // Mock dependencies
        BattleRepository mockedBattleRepository = mock(BattleRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        ELORepository mockedELORepository = mock(ELORepository.class);

        // Create an instance of BattleLogic with mocked dependencies
        BattleLogic battleLogic = new BattleLogic(mockedBattleRepository, mockedUserRepository, mockedCardRepository, mockedELORepository);

        // Create two user instances representing players in the battle
        User playerA = new User("playerAId", "playerA", "password", 100, 100, false);
        User playerB = new User("playerBId", "playerB", "password", 100, 100, false);

        // Define a battle ID
        String battleId = "battleId";

        // Configure the behavior of mockedBattleRepository to simulate a successful battle start
        when(mockedBattleRepository.startBattle(anyString(), anyString(), anyString())).thenReturn(true);

        // Mock that both players have the same deck (resulting in a draw)
        Card card = new Card("cardId", "Dragon", 10, "fire", "monster");
        when(mockedCardRepository.getUserDeckCards(playerA.getId())).thenReturn(new Card[]{card});
        when(mockedCardRepository.getUserDeckCards(playerB.getId())).thenReturn(new Card[]{card});

        // Perform the battle
        BattleResult result = battleLogic.performBattle(battleId, playerA, playerB);

        // Verify that the crownWinner method was called with the battle ID and null (no winner in a draw)
        verify(mockedBattleRepository).crownWinner(battleId, null);
    }


}
