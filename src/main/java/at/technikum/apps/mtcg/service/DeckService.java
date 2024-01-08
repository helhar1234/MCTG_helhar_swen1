package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository;

import java.sql.SQLException;

public class DeckService {
    private final CardRepository cardRepository;

    public DeckService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public boolean addCardToDeck(String userId, String cardId) throws SQLException {
        return cardRepository.addCardToDeck(userId, cardId);
    }

    public boolean resetDeck(String userId) throws SQLException {
        return cardRepository.resetDeck(userId);
    }

    public Card[] getUserDeckCards(String userId) throws SQLException {
        return cardRepository.getUserDeckCards(userId);
    }

    public boolean isCardInDeck(String userId, String cardId) throws SQLException {
        return cardRepository.isCardInDeck(userId, cardId);
    }

    public boolean hasDeckSet(String userId) throws SQLException {
        Card[] deckCards = cardRepository.getUserDeckCards(userId);
        return deckCards != null && deckCards.length == 4;
    }
}
