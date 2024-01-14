package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageServiceTest {

    @Test
    void savePackageShouldSucceedForAdminUserWithValidPackage() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        User adminUser = new User("adminUserId", "adminUser", "password", 5, 100, true);
        PackageCard[] packageCards = new PackageCard[5];
        for (int i = 0; i < packageCards.length; i++) {
            packageCards[i] = new PackageCard("cardId" + i, "cardName" + i, 10);
            when(mockedCardRepository.findCardById("cardId" + i)).thenReturn(Optional.empty());
        }

        when(mockedPackageRepository.savePackage(anyString())).thenReturn(true);
        when(mockedCardRepository.saveCard(any(PackageCard.class))).thenReturn(true);
        when(mockedPackageRepository.addCardToPackage(anyString(), anyString())).thenReturn(true);

        boolean result = packageService.savePackage(adminUser, packageCards);

        assertTrue(result);
        // Verify interactions with the repositories
    }

    @Test
    void savePackageShouldFailForNonAdminUser() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        User nonAdminUser = new User("userId", "username", "password", 5, 100, false);
        PackageCard[] packageCards = new PackageCard[5]; // Populate with valid cards

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> packageService.savePackage(nonAdminUser, packageCards)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getPackageByIdShouldReturnPackageIfExists() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        String packageId = "packageId";
        Package expectedPackage = new Package("packageId", 5, false);
        when(mockedPackageRepository.findPackageById(packageId)).thenReturn(Optional.of(expectedPackage));

        Optional<Package> result = packageService.getPackageById(packageId);

        assertTrue(result.isPresent());
        assertEquals(expectedPackage, result.get());
    }

    @Test
    void getRandomPackageShouldReturnPackageIdForUserNotPossessing() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        PackageService packageService = new PackageService(mockedCardRepository, mockedPackageRepository, mockedSessionService);

        String userId = "userId";
        String expectedPackageId = "packageId";
        when(mockedPackageRepository.getFirstPackageNotPossessing(userId)).thenReturn(expectedPackageId);
        String result = packageService.getRandomPackage(userId);

        assertNotNull(result);
        assertEquals(expectedPackageId, result);
    }
}

