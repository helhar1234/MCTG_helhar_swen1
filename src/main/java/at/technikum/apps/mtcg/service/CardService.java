package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.CardRepository;
import at.technikum.apps.mtcg.repository.CardRepository_db;

import java.util.Optional;

public class CardService {
    private final CardRepository cardRepository;

    public CardService() {
        this.cardRepository = new CardRepository_db();
    }

    public Optional<Card> findCardById(String id) {
        return cardRepository.findCardById(id);
    }

    public Card[] getUserCards(String userId) {
        return cardRepository.getUserCards(userId);
    }

    public Card[] getUserDeckCards(String userId) {
        return cardRepository.getUserDeckCards(userId);
    }

    public boolean isCardInStack(String userId, String cardId) {
        return cardRepository.isCardInStack(userId, cardId);
    }

    public boolean addCardToDeck(String userId, String cardId) {
        return cardRepository.addCardToDeck(userId, cardId);
    }

    public boolean resetDeck(String userId) {
        return cardRepository.resetDeck(userId);
    }

    public boolean isCardInDeck(String userId, String cardId) {
        return cardRepository.isCardInDeck(userId, cardId);
    }
}
