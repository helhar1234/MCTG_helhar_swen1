package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;

import java.sql.SQLException;
import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Optional<Card> findCardById(String id) throws SQLException {
        return cardRepository.findCardById(id);
    }

    public Card[] getUserCards(String userId) throws SQLException {
        return cardRepository.getUserCards(userId);
    }


    public boolean isCardInStack(String userId, String cardId) throws SQLException {
        return cardRepository.isCardInStack(userId, cardId);
    }


}
