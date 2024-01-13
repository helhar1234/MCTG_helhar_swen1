package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class CardService {
    private final CardRepository cardRepository;
    private final SessionService sessionService;

    public CardService(CardRepository cardRepository, SessionService sessionService) {
        this.cardRepository = cardRepository;
        this.sessionService = sessionService;
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


    public Card[] getCards(User user) {
        Card[] cards = getUserCards(user.getId());
        if (cards == null || cards.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "The user doesn't have any cards");
        }
        return cards;
    }
}
