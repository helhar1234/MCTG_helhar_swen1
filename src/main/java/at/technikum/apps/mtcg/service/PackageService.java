package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class PackageService {

    private final CardRepository cardRepository;
    private final PackageRepository packageRepository;
    private final SessionService sessionService;

    public PackageService(CardRepository cardRepository, PackageRepository packageRepository, SessionService sessionService) {
        this.cardRepository = cardRepository;
        this.packageRepository = packageRepository;
        this.sessionService = sessionService;
    }

    public boolean savePackage(User user, PackageCard[] packageCards) {

        if (!user.isAdmin()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }

        if (packageCards.length != 5) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "A package must contain exactly 5 cards");
        }
        Set<String> cardIds = new HashSet<>();
        for (PackageCard card : packageCards) {
            if (!cardIds.add(card.getId()) || cardRepository.findCardById(card.getId()).isPresent()) {
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Duplicate or existing cards found in the package");
            }
        }

        // Generate a new package ID
        String packageId = UUID.randomUUID().toString();

        // Save the package - assuming savePackage returns a boolean indicating success
        boolean isPackageSaved = packageRepository.savePackage(packageId);

        // Iterate over the cards array and save each card and add it to the package
        for (PackageCard packageCard : packageCards) {
            // Save the card - assuming saveCard returns a boolean indicating success
            boolean isCardSaved = cardRepository.saveCard(packageCard);

            // Add the card to the package - assuming addCardToPackage returns a boolean indicating success
            boolean isAddedToPackage = packageRepository.addCardToPackage(packageId, packageCard.getId());

        }

        return true;
    }

    public Optional<Package> getPackageById(String id) {
        return packageRepository.findPackageById(id);
    }

    public String getRandomPackage(String userId) {
        return packageRepository.getFirstPackageNotPossessing(userId);
    }

    public Optional<Package> getAvailablePackages(String packageId) {
        return packageRepository.getAvailablePackages(packageId);
    }
}
