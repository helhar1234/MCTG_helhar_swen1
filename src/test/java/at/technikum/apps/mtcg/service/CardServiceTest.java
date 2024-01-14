package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardServiceTest {

    @Test
    void findCardByIdShouldRetrieveFireElfCard() {
        // Mock dependencies and create an instance of CardService
        CardRepository mockedCardRepository = mock(CardRepository.class);

        // Define an optional FireElf card and configure the mock behavior for finding the card by ID
        Optional<Card> fireElf = Optional.of(new Card("elfCard", "FireElf", 100, "fire", "monster"));
        when(mockedCardRepository.findCardById("elfCard")).thenReturn(fireElf);

        // Create an instance of CardService and retrieve the card by ID
        CardService cardService = new CardService(mockedCardRepository);
        Optional<Card> foundCard = cardService.findCardById("elfCard");

        // Assertions to check if the FireElf card was retrieved correctly
        assertTrue(foundCard.isPresent());
        assertEquals("FireElf", foundCard.get().getName());
        assertEquals(100, foundCard.get().getDamage());
        assertEquals("fire", foundCard.get().getElementType());
    }


    @Test
    void getUserCardsShouldReturnThemedDeck() {
        // Mock dependencies and create an instance of CardService
        CardRepository mockedCardRepository = mock(CardRepository.class);
        // Define a themed deck and configure the mock behavior for retrieving user cards
        String userId = "user123";
        Card[] themedDeck = new Card[]{
                new Card("card1", "Goblin", 50, "fire", "monster"),
                new Card("card2", "WaterSpell", 40, "water", "spell"),
                new Card("card3", "Dragon", 60, "fire", "monster"),
                new Card("card3", "FireElf", 35, "fire", "monster")
        };
        when(mockedCardRepository.getUserCards(userId)).thenReturn(themedDeck);

        // Create an instance of CardService and retrieve the user's cards
        CardService cardService = new CardService(mockedCardRepository);
        Card[] retrievedCards = cardService.getUserCards(userId);

        // Assertions to check if the themed deck was retrieved correctly
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
        // Mock dependencies and create an instance of CardService
        CardRepository mockedCardRepository = mock(CardRepository.class);

        // Define a user ID and rare card ID, and configure the mock behavior for checking card existence
        String userId = "userCardServiceTest";
        String rareCardId = "cardCardServiceTest";
        when(mockedCardRepository.isCardInStack(userId, rareCardId)).thenReturn(true);

        // Create an instance of CardService and check if the card is in the user's stack
        CardService cardService = new CardService(mockedCardRepository);
        boolean isCardInStack = cardService.isCardInStack(userId, rareCardId);

        // Assertion to verify that the card is identified in the user's stack
        assertTrue(isCardInStack);
    }

    @Test
    void getCardsShouldThrowExceptionWhenUserHasNoCards() {
        // Mock dependencies and create an instance of CardService
        CardRepository mockedCardRepository = mock(CardRepository.class);

        // Define a user ID and configure the mock behavior for retrieving user cards (empty array)
        String userId = "user123";
        when(mockedCardRepository.getUserCards(userId)).thenReturn(new Card[0]);

        // Create an instance of CardService and attempt to retrieve cards for the user
        CardService cardService = new CardService(mockedCardRepository);
        User user = new User();
        user.setId(userId);

        // Assert that an HttpStatusException is thrown with a specific message
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> cardService.getCards(user)
        );

        // Additional assertions can be added here if needed, such as checking the exception's message
        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("The user doesn't have any cards", exception.getMessage());
    }


}
