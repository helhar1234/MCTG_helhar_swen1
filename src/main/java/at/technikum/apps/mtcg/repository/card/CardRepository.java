package at.technikum.apps.mtcg.repository.card;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.PackageCard;

import java.sql.SQLException;
import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public interface CardRepository {

    boolean saveCard(PackageCard packageCard) throws SQLException;

    Optional<Card> findCardById(String id) throws SQLException;

    Card[] getUserCards(String userId) throws SQLException;

    Card[] getUserDeckCards(String userId) throws SQLException;

    boolean isCardInStack(String userId, String cardId) throws SQLException;

    boolean addCardToDeck(String userId, String cardId) throws SQLException;

    boolean resetDeck(String userId) throws SQLException;

    boolean isCardInDeck(String userId, String cardId) throws SQLException;

    boolean deleteCardFromStack(String userId, String cardId) throws SQLException;

    boolean addCardToStack(String userId, String cardId) throws SQLException;
}
