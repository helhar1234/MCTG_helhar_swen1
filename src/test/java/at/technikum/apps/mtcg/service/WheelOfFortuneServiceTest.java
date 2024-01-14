package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.WheelPrize;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.wheel.WheelOfFortuneRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WheelOfFortuneServiceTest {

    @Test
    void shouldAwardCoinsOnSuccessfulSpin() {
        // Mock dependencies
        WheelOfFortuneRepository mockedWheelOfFortuneRepository = mock(WheelOfFortuneRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        Random mockedRandom = mock(Random.class);

        // Set up expected behavior
        when(mockedWheelOfFortuneRepository.hasUserSpun(anyString())).thenReturn(false);
        when(mockedRandom.nextInt(16)).thenReturn(1);

        // Create an instance of WheelOfFortuneService
        WheelOfFortuneService service = new WheelOfFortuneService(mockedWheelOfFortuneRepository, mockedUserRepository, mockedCardRepository, mockedCoinRepository, mockedRandom);

        // Create a user with coins
        User user = new User();
        user.setCoins(10);

        // Spin the wheel
        WheelPrize prize = service.spin(user);

        // Assertions
        assertNotNull(prize);
        assertEquals("COINS", prize.getPrizeType());
        assertTrue(prize.getCoinAmount() >= -5 && prize.getCoinAmount() <= 10);
        verify(mockedWheelOfFortuneRepository).saveSpin(user.getId());
    }

    @Test
    void shouldAwardCardOnSuccessfulSpin() {
        // Mock dependencies
        WheelOfFortuneRepository mockedWheelOfFortuneRepository = mock(WheelOfFortuneRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        CoinRepository mockedCoinRepository = mock(CoinRepository.class);
        Random mockedRandom = mock(Random.class);

        // Setting up the mocked behavior
        when(mockedWheelOfFortuneRepository.hasUserSpun(anyString())).thenReturn(false);
        Optional<Card> mockCard = Optional.of(new Card("cardId", "cardName", 12, "water", "spell"));
        when(mockedCardRepository.getCardNotPossesed()).thenReturn(mockCard);
        when(mockedRandom.nextInt(16)).thenReturn(10);

        // Create an instance of WheelOfFortuneService
        WheelOfFortuneService service = new WheelOfFortuneService(mockedWheelOfFortuneRepository, mockedUserRepository, mockedCardRepository, mockedCoinRepository, mockedRandom);

        User user = new User("1234", "testWheelService", "password");

        // Execute the method under test
        WheelPrize prize = service.spin(user);

        // Assertions
        assertNotNull(prize);
        assertEquals("CARD", prize.getPrizeType());
        assertNotNull(prize.getWonCard());
        assertEquals("cardId", prize.getWonCard().getId());
        verify(mockedWheelOfFortuneRepository).saveSpin(user.getId());
        verify(mockedUserRepository).addCardToStack(user.getId(), mockCard.get());
    }

    @Test
    void shouldThrowExceptionWhenUserHasAlreadySpun() {
        // Mock dependencies
        WheelOfFortuneRepository mockedWheelOfFortuneRepository = mock(WheelOfFortuneRepository.class);
        when(mockedWheelOfFortuneRepository.hasUserSpun(anyString())).thenReturn(true);

        // Create an instance of WheelOfFortuneService
        WheelOfFortuneService service = new WheelOfFortuneService(mockedWheelOfFortuneRepository, null, null, null, null);

        User user = new User("1234", "testWheelService", "password");

        // Attempt to spin when user has already spun
        HttpStatusException exception = assertThrows(HttpStatusException.class, () -> service.spin(user));

        // Assertions
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("testWheelService has already spun today - Try again tomorrow;)", exception.getMessage());
    }


}
