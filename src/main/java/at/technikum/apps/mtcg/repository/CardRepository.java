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
}
