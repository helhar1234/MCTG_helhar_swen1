package at.technikum.apps.mtcg.repository.card;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.PackageCard;

import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public interface CardRepository {

    boolean saveCard(PackageCard packageCard);

    Optional<Card> findCardById(String id);

    Card[] getUserCards(String userId);

    Card[] getUserDeckCards(String userId);

    boolean isCardInStack(String userId, String cardId);

    boolean addCardToDeck(String userId, String cardId);

    boolean resetDeck(String userId);

    boolean isCardInDeck(String userId, String cardId);

    boolean deleteCardFromStack(String userId, String cardId);

    boolean addCardToStack(String userId, String cardId);
}
