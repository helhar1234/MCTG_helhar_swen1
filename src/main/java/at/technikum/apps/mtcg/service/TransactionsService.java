package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;

import java.sql.SQLException;

public class TransactionsService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;

    public TransactionsService(CardRepository cardRepository, UserRepository userRepository, PackageRepository packageRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    public boolean makeTransaction(String packageId, String userId, int price) throws SQLException {
        // Attempt to deduct the price from the user's coins
        boolean coinsUpdated = userRepository.updateCoins(userId, -price); // Assuming this method deducts coins when given a negative amount

        if (!coinsUpdated) {
            // If updating coins was not successful, do not proceed and return false
            return false;
        }
        // Retrieve the cards from the package
        Card[] cards = packageRepository.getPackageCardsById(packageId);

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
        packageRepository.deletePackage(packageId);

        // If all cards are successfully added to the user's stack, return true
        return true;
    }
}
