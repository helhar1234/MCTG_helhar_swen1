package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.repository.CardRepository;
import at.technikum.apps.mtcg.repository.CardRepository_db;

import java.util.UUID;

public class PackageService {

    private final CardRepository cardRepository;

    public PackageService() {
        this.cardRepository = new CardRepository_db();
    }
    public boolean savePackage(PackageCard[] packageCards) {
        // Generate a new package ID
        String packageId = UUID.randomUUID().toString();

        // Save the package - assuming savePackage returns a boolean indicating success
        boolean isPackageSaved = cardRepository.savePackage(packageId);
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
            boolean isAddedToPackage = cardRepository.addCardToPackage(packageId, packageCard.getId());
            if (!isAddedToPackage) {
                // If adding any card to the package fails, return false
                return false;
            }
        }

        // If all cards are saved and added to the package successfully, return true
        return true;
    }
}
