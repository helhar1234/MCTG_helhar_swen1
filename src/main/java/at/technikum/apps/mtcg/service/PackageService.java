package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.repository.CardRepository;
import at.technikum.apps.mtcg.repository.CardRepository_db;
import at.technikum.apps.mtcg.repository.UserRepository;
import at.technikum.apps.mtcg.repository.UserRepository_db;

import java.util.Optional;
import java.util.UUID;

public class PackageService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public PackageService() {
        this.cardRepository = new CardRepository_db();
        this.userRepository = new UserRepository_db();
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

    public Optional<Package> getPackageById(String id) {
        return cardRepository.findPackageById(id);
    }

    public boolean makeTransaction(String packageId, String userId, int price) {
        // Attempt to deduct the price from the user's coins
        boolean coinsUpdated = userRepository.updateCoins(userId, -price); // Assuming this method deducts coins when given a negative amount

        if (!coinsUpdated) {
            // If updating coins was not successful, do not proceed and return false
            return false;
        }
        // Retrieve the cards from the package
        Card[] cards = cardRepository.getPackageCardsById(packageId);

        if (cards == null || cards.length != 5) {
            // If the retrieval failed or the number of cards is not correct, reverse the coin update and return false
            userRepository.updateCoins(userId, price); // Reverse the coin deduction since the transaction failed
            return false;
        }

        // Add each card to the user's stack
        for (Card card : cards) {
            boolean cardAdded = userRepository.addCardToStack(userId, card);
            if (!cardAdded) {
                // If adding any card to the user's stack fails, reverse the coin update and return false
                userRepository.updateCoins(userId, price); // Reverse the coin deduction since the transaction failed
                return false;
            }
        }

        // If all cards are successfully added to the user's stack, return true
        return true;
    }

}
