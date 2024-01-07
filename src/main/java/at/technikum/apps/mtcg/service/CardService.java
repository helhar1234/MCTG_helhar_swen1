package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;

import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
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


    public boolean isCardInStack(String userId, String cardId) {
        return cardRepository.isCardInStack(userId, cardId);
    }


}
