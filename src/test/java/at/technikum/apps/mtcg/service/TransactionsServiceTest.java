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
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionsServiceTest {

    @Test
    void makeTransactionShouldSucceedWithSufficientCoinsAndAvailablePackage() {
        // Mock setup
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedUserRepository, mockedPackageRepository, mockedCoinRepository);

        // Create a user with sufficient coins and a package to purchase
        User user = new User("userId", "username", "password", 5, 100, false);
        String packageId = "packageId";
        Package aPackage = new Package("packageId", 5, false);
        Card[] cards = new Card[5]; // Create an array of 5 cards for the package
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Card("cardId" + i, "cardName" + i, 10, "element", "type");
            when(mockedUserRepository.addCardToStack(user.getId(), cards[i])).thenReturn(true);
        }

        // Configure mock behaviors for package, cards, and coins
        when(mockedPackageRepository.getAvailablePackages(packageId)).thenReturn(Optional.of(aPackage));
        when(mockedPackageRepository.getPackageCardsById(packageId)).thenReturn(cards);
        when(mockedCoinRepository.updateCoins(user.getId(), -5)).thenReturn(true);

        // Execute the method under test
        boolean result = transactionsService.makeTransaction(user, packageId);

        // Assertions
        assertTrue(result);
        verify(mockedCoinRepository).updateCoins(user.getId(), -5);
        verify(mockedPackageRepository).deletePackage(packageId);
        for (Card card : cards) {
            verify(mockedUserRepository).addCardToStack(user.getId(), card);
        }
    }

    @Test
    void makeTransactionShouldFailWithInsufficientCoins() {
        // Mock setup
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedUserRepository, mockedPackageRepository, mockedCoinRepository);

        // Create a user with insufficient coins and a package with a higher price
        User user = new User("userId", "username", "password", 30, 100, false); // Assume 30 coins
        String packageId = "packageId";
        Package aPackage = new Package(packageId, 50, false); // Assume package price is 50 coins
        when(mockedPackageRepository.getAvailablePackages(packageId)).thenReturn(Optional.of(aPackage));

        // Execute the method under test and expect an exception
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> transactionsService.makeTransaction(user, packageId)
        );

        // Assertions
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Not enough money for buying a card package", exception.getMessage());
    }

    @Test
    void executeTransactionShouldFailAndRefundWhenCardAdditionFails() {
        // Mock setup
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedUserRepository, mockedPackageRepository, mockedCoinRepository);

        String packageId = "packageId";
        String userId = "userId";
        int price = 50;
        Card[] cards = new Card[5]; // Populate with valid cards
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Card("cardId" + i, "cardName" + i, 10, "element", "type");
        }
        when(mockedCoinRepository.updateCoins(userId, -price)).thenReturn(true);
        when(mockedPackageRepository.getPackageCardsById(packageId)).thenReturn(cards);
        when(mockedUserRepository.addCardToStack(eq(userId), any(Card.class))).thenReturn(true, false); // Simulate failure on the second card

        // Execute the method under test and expect an exception
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> transactionsService.executeTransaction(packageId, userId, price)
        );

        // Assertions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Error while adding cards to your Stack - Please try again!", exception.getMessage());
        verify(mockedCoinRepository).updateCoins(userId, price); // Verify coins are refunded
    }

}

