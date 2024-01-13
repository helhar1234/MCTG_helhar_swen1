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

    public boolean addCardToDeck(String userId, String cardId) {
        return cardRepository.addCardToDeck(userId, cardId);
    }

    public boolean resetDeck(String userId) {
        return cardRepository.resetDeck(userId);
    }

    public Card[] getUserDeckCards(String userId) {
        return cardRepository.getUserDeckCards(userId);
    }

    public boolean isCardInDeck(String userId, String cardId) {
        return cardRepository.isCardInDeck(userId, cardId);
    }

    public boolean hasDeckSet(String userId) {
        Card[] deckCards = cardRepository.getUserDeckCards(userId);
        return deckCards != null && deckCards.length == 4;
    }

    public boolean configureDeck(User user, String[] cardIds) {
        if (cardIds == null || cardIds.length != 4) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Exactly four card IDs are required");
        }

        for (String cardId : cardIds) {
            if (!cardRepository.isCardInStack(user.getId(), cardId)) {
                throw new HttpStatusException(HttpStatus.FORBIDDEN, "One or more cards do not belong to the user");
            }
        }

        boolean isDeckReset = resetDeck(user.getId());
        boolean areCardsAdded = true;
        for (String cardId : cardIds) {
            areCardsAdded &= addCardToDeck(user.getId(), cardId);
        }

        if (!isDeckReset || !areCardsAdded) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding to Deck - Try again!");
        }

        return true;
    }

    public Card[] getDeck(User user) {
        Card[] cards = getUserDeckCards(user.getId());
        if (cards == null || cards.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "The user doesn't have any cards");
        }

        return cards;
    }
}
