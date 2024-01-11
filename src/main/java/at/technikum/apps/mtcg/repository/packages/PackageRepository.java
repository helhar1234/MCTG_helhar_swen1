package at.technikum.apps.mtcg.repository.packages;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;

import java.util.Optional;

public interface PackageRepository {
    Optional<Package> findPackageById(String id);

    boolean savePackage(String id);

    Card[] getPackageCardsById(String packageId);

    Optional<Package> getAvailablePackages(String packageId);

    String getFirstPackageNotPossessing(String userId);

    boolean addCardToPackage(String packageId, String cardId);

    boolean deletePackage(String packageId);
}
