package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.server.http.HttpStatus;

public class DeckService {
    private final CardRepository cardRepository;
    private final SessionService sessionService;

    public DeckService(CardRepository cardRepository, SessionService sessionService) {
        this.cardRepository = cardRepository;
        this.sessionService = sessionService;
    }

    /**
     * Adds a card to a user's deck.
     *
     * @param userId The ID of the user.
     * @param cardId The ID of the card to be added to the deck.
     * @return True if the card is successfully added to the deck, false otherwise.
     */
    public boolean addCardToDeck(String userId, String cardId) {
        // Delegate to the cardRepository to add the card to the user's deck
        return cardRepository.addCardToDeck(userId, cardId);
    }


    /**
     * Resets (clears) the deck of a user.
     *
     * @param userId The ID of the user whose deck is to be reset.
     * @return True if the deck is successfully reset, false otherwise.
     */
    public boolean resetDeck(String userId) {
        // Delegate to the cardRepository to reset the user's deck
        return cardRepository.resetDeck(userId);
    }


    /**
     * Retrieves the cards currently in a user's deck.
     *
     * @param userId The ID of the user.
     * @return An array of Cards that are in the user's deck.
     */
    public Card[] getUserDeckCards(String userId) {
        // Delegate to the cardRepository to get the cards in the user's deck
        return cardRepository.getUserDeckCards(userId);
    }


    /**
     * Checks if a specific card is in a user's deck.
     *
     * @param userId The ID of the user.
     * @param cardId The ID of the card to check.
     * @return True if the card is in the deck, false otherwise.
     */
    public boolean isCardInDeck(String userId, String cardId) {
        // Delegate to the cardRepository to check if the card is in the user's deck
        return cardRepository.isCardInDeck(userId, cardId);
    }


    /**
     * Checks if a user has a complete set of cards in their deck.
     *
     * @param userId The ID of the user.
     * @return True if the user's deck has exactly 4 cards, false otherwise.
     */
    public boolean hasDeckSet(String userId) {
        // Retrieve the cards in the user's deck
        Card[] deckCards = cardRepository.getUserDeckCards(userId);

        // Check if the deck is set with exactly 4 cards
        return deckCards != null && deckCards.length == 4;
    }


    /**
     * Configures a user's deck with a specified set of cards.
     *
     * @param user    The user whose deck is being configured.
     * @param cardIds An array of card IDs to set in the deck.
     * @return True if the deck is successfully configured, false otherwise.
     * @throws HttpStatusException If the number of cards is not 4 or if any card does not belong to the user.
     */
    public boolean configureDeck(User user, String[] cardIds) {
        // Validate the card IDs
        if (cardIds == null || cardIds.length != 4) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Exactly four card IDs are required");
        }

        // Check if each card belongs to the user
        for (String cardId : cardIds) {
            if (!cardRepository.isCardInStack(user.getId(), cardId)) {
                throw new HttpStatusException(HttpStatus.FORBIDDEN, "One or more cards do not belong to the user");
            }
        }

        // Reset the user's deck and add the new cards
        boolean isDeckReset = resetDeck(user.getId());
        boolean areCardsAdded = true;
        for (String cardId : cardIds) {
            areCardsAdded &= addCardToDeck(user.getId(), cardId);
        }

        // Validate if the deck was reset and cards were added successfully
        if (!isDeckReset || !areCardsAdded) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding to Deck - Try again!");
        }

        return true;
    }


    /**
     * Retrieves the deck of a user.
     *
     * @param user The user whose deck is to be retrieved.
     * @return An array of Cards that are in the user's deck.
     * @throws HttpStatusException If the user does not have any cards in their deck.
     */
    public Card[] getDeck(User user) {
        // Retrieve the cards in the user's deck
        Card[] cards = getUserDeckCards(user.getId());

        // Check if the user has any cards in their deck
        if (cards == null || cards.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "The user doesn't have any cards");
        }

        return cards;
    }

}
