package at.technikum.apps.mtcg.repository.packages;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;

import java.sql.SQLException;
import java.util.Optional;

public interface PackageRepository {
    Optional<Package> findPackageById(String id) throws SQLException;

    boolean savePackage(String id) throws SQLException;

    Card[] getPackageCardsById(String packageId) throws SQLException;

    Optional<Package> getAvailablePackages(String packageId) throws SQLException;

    String getFirstPackageNotPossessing(String userId) throws SQLException;

    boolean addCardToPackage(String packageId, String cardId) throws SQLException;

    boolean deletePackage(String packageId) throws SQLException;
}
