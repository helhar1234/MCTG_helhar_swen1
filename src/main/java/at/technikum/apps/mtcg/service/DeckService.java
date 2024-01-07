package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;

public class DeckService {
    private final CardRepository cardRepository;

    public DeckService() {
        this.cardRepository = new CardRepository_db();
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
}
