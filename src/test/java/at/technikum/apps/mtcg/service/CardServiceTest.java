package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class CardServiceTest {

    @Test
    void findCardByIdShouldRetrieveFireElfCard() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);

        Optional<Card> fireElf = Optional.of(new Card("elfCard", "FireElf", 100, "fire", "monster"));
        when(mockedCardRepository.findCardById("elfCard")).thenReturn(fireElf);

        CardService cardService = new CardService(mockedCardRepository, mockedSessionService);
        Optional<Card> foundCard = cardService.findCardById("elfCard");

        assertTrue(foundCard.isPresent());
        assertEquals("FireElf", foundCard.get().getName());
        assertEquals(100, foundCard.get().getDamage());
        assertEquals("fire", foundCard.get().getElementType());
    }


    @Test
    void getUserCardsShouldReturnThemedDeck() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        String userId = "user123";
        Card[] themedDeck = new Card[]{
                new Card("card1", "Goblin", 50, "fire", "monster"),
                new Card("card2", "WaterSpell", 40, "water", "spell"),
                new Card("card3", "Dragon", 60, "fire", "monster"),
                new Card("card3", "FireElf", 35, "fire", "monster")
        };

        when(mockedCardRepository.getUserCards(userId)).thenReturn(themedDeck);

        CardService cardService = new CardService(mockedCardRepository, mockedSessionService);
        Card[] retrievedCards = cardService.getUserCards(userId);

        assertEquals(themedDeck.length, retrievedCards.length);
        for (int i = 0; i < retrievedCards.length; i++) {
            assertEquals(themedDeck[i].getId(), retrievedCards[i].getId());
            assertEquals(themedDeck[i].getName(), retrievedCards[i].getName());
            assertEquals(themedDeck[i].getDamage(), retrievedCards[i].getDamage());
            assertEquals(themedDeck[i].getElementType(), retrievedCards[i].getElementType());
            assertEquals(themedDeck[i].getCardType(), retrievedCards[i].getCardType());
        }

    }

    @Test
    void isCardInStackShouldIdentifyCard() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);

        String userId = "userCardServiceTest";
        String rareCardId = "cardCardServiceTest";

        when(mockedCardRepository.isCardInStack(userId, rareCardId)).thenReturn(true);

        CardService cardService = new CardService(mockedCardRepository, mockedSessionService);
        boolean isCardInStack = cardService.isCardInStack(userId, rareCardId);

        assertTrue(isCardInStack);
    }

    @Test
    void getCardsShouldThrowExceptionWhenUserHasNoCards() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);

        String userId = "user123";
        when(mockedCardRepository.getUserCards(userId)).thenReturn(new Card[0]);

        CardService cardService = new CardService(mockedCardRepository, mockedSessionService);

        User user = new User();
        user.setId(userId);

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> cardService.getCards(user)
        );

        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("The user doesn't have any cards", exception.getMessage());
    }


}
