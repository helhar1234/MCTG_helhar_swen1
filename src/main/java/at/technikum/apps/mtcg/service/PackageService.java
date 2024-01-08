package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class PackageService {

    private final CardRepository cardRepository;
    private final PackageRepository packageRepository;

    public PackageService(CardRepository cardRepository, PackageRepository packageRepository) {
        this.cardRepository = cardRepository;
        this.packageRepository = packageRepository;
    }

    public synchronized boolean savePackage(PackageCard[] packageCards) throws SQLException {
        // Generate a new package ID
        String packageId = UUID.randomUUID().toString();

        // Save the package - assuming savePackage returns a boolean indicating success
        boolean isPackageSaved = packageRepository.savePackage(packageId);
        if (!isPackageSaved) {
            return false;
        }

        // Iterate over the cards array and save each card and add it to the package
        for (PackageCard packageCard : packageCards) {
            // Save the card - assuming saveCard returns a boolean indicating success
            boolean isCardSaved = cardRepository.saveCard(packageCard);
            if (!isCardSaved) {
                // If saving any card fails, return false
                return false;
            }

            // Add the card to the package - assuming addCardToPackage returns a boolean indicating success
            boolean isAddedToPackage = packageRepository.addCardToPackage(packageId, packageCard.getId());
            if (!isAddedToPackage) {
                // If adding any card to the package fails, return false
                return false;
            }
        }

        // If all cards are saved and added to the package successfully, return true
        return true;
    }

    public Optional<Package> getPackageById(String id) throws SQLException {
        return packageRepository.findPackageById(id);
    }

    public String getRandomPackage(String userId) throws SQLException {
        return packageRepository.getFirstPackageNotPossessing(userId);
    }

    public Optional<Package> getAvailablePackages(String packageId) throws SQLException {
        return packageRepository.getAvailablePackages(packageId);
    }
}
