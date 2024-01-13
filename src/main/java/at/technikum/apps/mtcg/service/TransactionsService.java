package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;

public class TransactionsService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final SessionService sessionService;
    private final CoinRepository coinRepository;

    public TransactionsService(CardRepository cardRepository, UserRepository userRepository, PackageRepository packageRepository, SessionService sessionService, CoinRepository coinRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.sessionService = sessionService;
        this.coinRepository = coinRepository;
    }

    public boolean executeTransaction(String packageId, String userId, int price) {
        // Attempt to deduct the price from the user's coins
        boolean coinsUpdated = coinRepository.updateCoins(userId, -price);

        if (!coinsUpdated) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while payment - Please try again!");
        }
        // Retrieve the cards from the package
        Card[] cards = packageRepository.getPackageCardsById(packageId);

        if (cards == null || cards.length != 5) {
            // If the retrieval failed or the number of cards is not correct, reverse the coin update and return false
            coinRepository.updateCoins(userId, price); // Reverse the coin deduction since the transaction failed
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving Package cards - Please try again!");
        }

        // Add each card to the user's stack
        for (Card card : cards) {
            boolean cardAdded = userRepository.addCardToStack(userId, card);
            if (!cardAdded) {
                // If adding any card to the user's stack fails, reverse the coin update and return false
                coinRepository.updateCoins(userId, price); // Reverse the coin deduction since the transaction failed
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding cards to your Stack - Please try again!");
            }
        }
        packageRepository.deletePackage(packageId);

        // If all cards are successfully added to the user's stack, return true
        return true;
    }

    public boolean makeTransaction(User user, String packageId) {
        if (packageId == null) {
            packageId = packageRepository.getFirstPackageNotPossessing(user.getId());
            if (packageId == null) {
                throw new HttpStatusException(HttpStatus.CONFLICT, "No available packages for buying");
            }
        }

        Optional<Package> aPackage = packageRepository.getAvailablePackages(packageId);

        if (aPackage.isEmpty()) {
            throw new HttpStatusException(HttpStatus.CONFLICT, "No available packages for buying");
        }

        if (user.getCoins() < aPackage.get().getPrice()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not enough money for buying a card package");
        }

        return executeTransaction(packageId, user.getId(), aPackage.get().getPrice());
    }
}
