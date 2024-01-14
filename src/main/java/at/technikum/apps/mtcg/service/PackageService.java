package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.server.http.HttpStatus;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class PackageService {

    private final CardRepository cardRepository;
    private final PackageRepository packageRepository;

    public PackageService(CardRepository cardRepository, PackageRepository packageRepository) {
        this.cardRepository = cardRepository;
        this.packageRepository = packageRepository;
    }

    /**
     * Saves a new package of cards to the repository.
     *
     * @param user         The admin user attempting to save the package.
     * @param packageCards The array of cards to be included in the package.
     * @return True if the package is successfully saved, false otherwise.
     * @throws HttpStatusException If the user is not an admin, the package doesn't contain exactly 5 cards, or if there are duplicate or pre-existing cards.
     */
    public boolean savePackage(User user, PackageCard[] packageCards) {
        // Check if the user is an admin
        if (!user.isAdmin()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }

        // Validate the package size
        if (packageCards.length != 5) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "A package must contain exactly 5 cards");
        }

        // Check for duplicate or pre-existing cards in the package
        Set<String> cardIds = new HashSet<>();
        for (PackageCard card : packageCards) {
            if (!cardIds.add(card.getId()) || cardRepository.findCardById(card.getId()).isPresent()) {
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Duplicate or existing cards found in the package");
            }
        }

        // Generate a new unique package ID
        String packageId = UUID.randomUUID().toString();

        // Save the package and each card in the package
        boolean isPackageSaved = packageRepository.savePackage(packageId);
        for (PackageCard packageCard : packageCards) {
            cardRepository.saveCard(packageCard);
            packageRepository.addCardToPackage(packageId, packageCard.getId());
        }

        return true;
    }


    /**
     * Retrieves a package by its unique ID.
     *
     * @param id The unique ID of the package.
     * @return An Optional containing the Package if found, or an empty Optional if not found.
     */
    public Optional<Package> getPackageById(String id) {
        // Delegate to the packageRepository to find the package by ID
        return packageRepository.findPackageById(id);
    }


    /**
     * Retrieves the first package that the specified user does not possess.
     *
     * @param userId The ID of the user.
     * @return The ID of the first package not possessed by the user.
     */
    public String getRandomPackage(String userId) {
        // Delegate to the packageRepository to find the first package not owned by the user
        return packageRepository.getFirstPackageNotPossessing(userId);
    }


    /**
     * Retrieves available packages based on a package ID.
     *
     * @param packageId The ID of the package to be retrieved.
     * @return An Optional containing the Package if it's available, or an empty Optional if not.
     */
    public Optional<Package> getAvailablePackages(String packageId) {
        // Delegate to the packageRepository to get available packages based on the provided ID
        return packageRepository.getAvailablePackages(packageId);
    }

}
