package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.PackageCard;

import java.util.Optional;

public interface CardRepository {
    boolean savePackage(String id);

    boolean saveCard(PackageCard packageCard);

    boolean addCardToPackage(String packageId, String cardId);

    Optional<Card> findCardById(String id);

    Optional<Package> findPackageById(String id);

    Card[] getPackageCardsById(String packageId);

    Card[] getUserCards(String userId);

    Card[] getUserDeckCards(String userId);

    boolean isCardInStack(String userId, String cardId);

    boolean addCardToDeck(String userId, String cardId);

    boolean resetDeck(String userId);

    String getFirstPackageNotPossessing(String userId);

    boolean deletePackage(String packageId);

    Optional<Package> getAvailablePackages(String packageId);

    boolean isCardInDeck(String userId, String cardId);

    boolean deleteCardFromStack(String userId, String cardId);

    boolean addCardToStack(String userId, String cardId);
}
