package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionsServiceTest {

    @Test
    void makeTransactionShouldSucceedWithSufficientCoinsAndAvailablePackage() {
        // Mock setup
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedCardRepository, mockedUserRepository, mockedPackageRepository, mockedSessionService, mockedCoinRepository);

        User user = new User("userId", "username", "password", 5, 100, false);
        String packageId = "packageId";
        Package aPackage = new Package("packageId", 5, false);
        Card[] cards = new Card[5]; // Create an array of 5 cards for the package
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Card("cardId" + i, "cardName" + i, 10, "element", "type");
            when(mockedCardRepository.findCardById("cardId" + i)).thenReturn(Optional.empty());
            when(mockedUserRepository.addCardToStack(user.getId(), cards[i])).thenReturn(true);
        }

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
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedCardRepository, mockedUserRepository, mockedPackageRepository, mockedSessionService, mockedCoinRepository);

        User user = new User("userId", "username", "password", 30, 100, false); // Assume 30 coins
        String packageId = "packageId";
        Package aPackage = new Package(packageId, 50, false); // Assume package price is 50 coins
        when(mockedPackageRepository.getAvailablePackages(packageId)).thenReturn(Optional.of(aPackage));

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> transactionsService.makeTransaction(user, packageId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Not enough money for buying a card package", exception.getMessage());
    }


    @Test
    void executeTransactionShouldFailAndRefundWhenCardAdditionFails() {
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        PackageRepository mockedPackageRepository = mock(PackageRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        TransactionsService transactionsService = new TransactionsService(mockedCardRepository, mockedUserRepository, mockedPackageRepository, mockedSessionService, mockedCoinRepository);

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

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> transactionsService.executeTransaction(packageId, userId, price)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Error while adding cards to your Stack - Please try again!", exception.getMessage());
        verify(mockedCoinRepository).updateCoins(userId, price); // Verify coins are refunded
    }
}

