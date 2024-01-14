package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;

public class TransactionsService {
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final CoinRepository coinRepository;

    public TransactionsService(UserRepository userRepository, PackageRepository packageRepository, CoinRepository coinRepository) {
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.coinRepository = coinRepository;
    }

    /**
     * Executes a transaction for buying a package.
     *
     * @param packageId The ID of the package being bought.
     * @param userId    The ID of the user buying the package.
     * @param price     The price of the package.
     * @return True if the transaction is successful, false otherwise.
     * @throws HttpStatusException If there is an error during the payment process or while adding cards.
     */
    public boolean executeTransaction(String packageId, String userId, int price) {
        // Deduct the price from the user's coins
        boolean coinsUpdated = coinRepository.updateCoins(userId, -price);

        // Check if the coin deduction was successful
        if (!coinsUpdated) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while payment - Please try again!");
        }

        // Retrieve the cards included in the package
        Card[] cards = packageRepository.getPackageCardsById(packageId);

        // Verify the correct number of cards is retrieved
        if (cards == null || cards.length != 5) {
            coinRepository.updateCoins(userId, price); // Reverse the coin deduction
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving Package cards - Please try again!");
        }

        // Add each card from the package to the user's stack
        for (Card card : cards) {
            boolean cardAdded = userRepository.addCardToStack(userId, card);
            if (!cardAdded) {
                coinRepository.updateCoins(userId, price); // Reverse the coin deduction
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding cards to your Stack - Please try again!");
            }
        }

        // Delete the package after successful transaction
        packageRepository.deletePackage(packageId);

        return true; // Transaction was successful
    }


    /**
     * Initiates a transaction for a user to buy a package.
     *
     * @param user      The user who is buying the package.
     * @param packageId The ID of the package to be bought. If null, the first available package is selected.
     * @return True if the transaction is successful, false otherwise.
     * @throws HttpStatusException If there are no available packages or the user doesn't have enough coins.
     */
    public boolean makeTransaction(User user, String packageId) {
        // If no specific package ID is provided, get the first available package
        if (packageId == null) {
            packageId = packageRepository.getFirstPackageNotPossessing(user.getId());
            if (packageId == null) {
                throw new HttpStatusException(HttpStatus.CONFLICT, "No available packages for buying");
            }
        }

        // Get the details of the available package
        Optional<Package> aPackage = packageRepository.getAvailablePackages(packageId);

        // Check if the package is available
        if (aPackage.isEmpty()) {
            throw new HttpStatusException(HttpStatus.CONFLICT, "No available packages for buying");
        }

        // Check if the user has enough coins to buy the package
        if (user.getCoins() < aPackage.get().getPrice()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not enough money for buying a card package");
        }

        // Execute the transaction for the selected package
        return executeTransaction(packageId, user.getId(), aPackage.get().getPrice());
    }

}
