package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.elo.ELORepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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

        BattleLogic battleLogic = new BattleLogic(mockedBattleRepository, mockedUserRepository, mockedCardRepository, mockedELORepository);

        User playerA = new User("playerAId", "playerA", "password", 100, 100, false);
        User playerB = new User("playerBId", "playerB", "password", 100, 100, false);

        String battleId = "battleId";
        when(mockedBattleRepository.startBattle(anyString(), anyString(), anyString())).thenReturn(true);

        // Mock, dass beide Spieler das gleiche Deck haben (was zu einem Unentschieden führt)
        Card card = new Card("cardId", "Dragon", 10, "fire", "monster");
        when(mockedCardRepository.getUserDeckCards(playerA.getId())).thenReturn(new Card[]{card});
        when(mockedCardRepository.getUserDeckCards(playerB.getId())).thenReturn(new Card[]{card});

        // Führe die Schlacht durch
        BattleResult result = battleLogic.performBattle(battleId, playerA, playerB);

        // Überprüfe, ob crownWinner korrekt aufgerufen wurde
        verify(mockedBattleRepository).crownWinner(battleId, null);
    }

}
