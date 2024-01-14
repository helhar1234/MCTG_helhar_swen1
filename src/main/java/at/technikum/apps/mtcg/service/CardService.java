package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;

public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Finds a card by its unique ID.
     *
     * @param id The unique identifier of the card.
     * @return An Optional containing the Card if found, or an empty Optional if the card is not found.
     */
    public Optional<Card> findCardById(String id) {
        // Delegate to the cardRepository to find the card by its ID
        return cardRepository.findCardById(id);
    }


    /**
     * Retrieves all cards owned by a user.
     *
     * @param userId The unique identifier of the user.
     * @return An array of Cards owned by the user.
     */
    public Card[] getUserCards(String userId) {
        // Delegate to the cardRepository to get all cards owned by the user
        return cardRepository.getUserCards(userId);
    }


    /**
     * Checks if a specific card is in a user's stack.
     *
     * @param userId The ID of the user.
     * @param cardId The ID of the card to check.
     * @return True if the card is in the user's stack, false otherwise.
     */
    public boolean isCardInStack(String userId, String cardId) {
        // Delegate to the cardRepository to check if the card is in the user's stack
        return cardRepository.isCardInStack(userId, cardId);
    }


    /**
     * Retrieves the cards associated with a specific user.
     *
     * @param user The user whose cards are to be retrieved.
     * @return An array of Cards associated with the user.
     * @throws HttpStatusException If the user does not have any cards.
     */
    public Card[] getCards(User user) {
        // Retrieve the cards owned by the user
        Card[] cards = getUserCards(user.getId());

        // Check if the user owns any cards
        if (cards == null || cards.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "The user doesn't have any cards");
        }

        return cards;
    }

}
