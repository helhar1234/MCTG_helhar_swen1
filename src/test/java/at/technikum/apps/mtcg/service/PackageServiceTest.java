package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackageServiceTest {

    @Test
    void savePackageShouldSucceedForAdminUserWithValidPackage() {
        // Mock dependencies and create an instance of PackageService
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        // Create an admin user and package cards
        User adminUser = new User("adminUserId", "adminUser", "password", 5, 100, true);
        PackageCard[] packageCards = new PackageCard[5];
        for (int i = 0; i < packageCards.length; i++) {
            packageCards[i] = new PackageCard("cardId" + i, "cardName" + i, 10);
            when(mockedCardRepository.findCardById("cardId" + i)).thenReturn(Optional.empty());
        }
// Configure mock behaviors for card and package repositories
        when(mockedPackageRepository.savePackage(anyString())).thenReturn(true);
        when(mockedCardRepository.saveCard(any(PackageCard.class))).thenReturn(true);
        when(mockedPackageRepository.addCardToPackage(anyString(), anyString())).thenReturn(true);

        // Call the method under test and assert that it returns true
        boolean result = packageService.savePackage(adminUser, packageCards);
        assertTrue(result);
    }

    @Test
    void savePackageShouldFailForNonAdminUser() {
        // Mock dependencies and create an instance of PackageService
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        // Create a non-admin user and package cards
        User nonAdminUser = new User("userId", "username", "password", 5, 100, false);
        PackageCard[] packageCards = new PackageCard[5]; // Populate with valid cards

        // Assert that an HttpStatusException is thrown with a specific status code
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> packageService.savePackage(nonAdminUser, packageCards)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getPackageByIdShouldReturnPackageIfExists() {
        // Mock dependencies and create an instance of PackageService
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        // Define a package ID and the expected package
        String packageId = "packageId";
        Package expectedPackage = new Package("packageId", 5, false);
        // Configure mock behavior for finding the package by ID
        when(mockedPackageRepository.findPackageById(packageId)).thenReturn(Optional.of(expectedPackage));

        // Call the method under test and assert that it returns the expected package
        Optional<Package> result = packageService.getPackageById(packageId);
        assertTrue(result.isPresent());
        assertEquals(expectedPackage, result.get());
    }

    @Test
    void getRandomPackageShouldReturnPackageIdForUserNotPossessing() {
        // Mock dependencies and create an instance of PackageService
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        // Define a user ID and the expected package ID
        String userId = "userId";
        String expectedPackageId = "packageId";
        // Configure mock behavior for finding the first package not possessed by the user
        when(mockedPackageRepository.getFirstPackageNotPossessing(userId)).thenReturn(expectedPackageId);
        // Call the method under test and assert that it returns the expected package ID
        String result = packageService.getRandomPackage(userId);
        assertNotNull(result);
        assertEquals(expectedPackageId, result);
    }
}

