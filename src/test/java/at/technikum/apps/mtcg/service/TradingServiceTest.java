package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradingServiceTest {

    @Test
    void createTradeShouldSucceedWithValidConditions() {
        // Mock dependencies and create an instance of TradingService
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository);

        // Create a user and a trade request
        User user = new User("userId", "username", "password");
        TradeRequest tradeRequest = new TradeRequest("tradeId", "cardId", "type", 10);

        // Configure mock behaviors for card checks and trade creation
        when(mockedCardRepository.isCardInStack(user.getId(), tradeRequest.getCardToTrade())).thenReturn(true);
        when(mockedCardRepository.isCardInDeck(tradeRequest.getCardToTrade(), user.getId())).thenReturn(false);
        when(mockedTradingRepository.getTradeById(tradeRequest.getId())).thenReturn(Optional.empty());
        when(mockedTradingRepository.createTrade(tradeRequest, user.getId())).thenReturn(true);

        // Call the method under test and assert success
        boolean result = tradingService.createTrade(user, tradeRequest);
        assertTrue(result);
    }

    @Test
    void getAllTradesShouldThrowExceptionWhenNoTradesExist() {
        // Mock dependencies and create an instance of TradingService
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository);

        // Create a user
        User user = new User("userId", "username", "password");

        // Configure mock behavior to return an empty array of trades
        when(mockedTradingRepository.getAllTrades()).thenReturn(new TradeRequest[0]);

        // Assert that an HttpStatusException is thrown with specific details
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.getAllTrades(user)
        );
        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("No Trading Deals", exception.getMessage());
    }

    @Test
    void deleteTradeShouldThrowExceptionForUnauthorizedUser() {
        // Mock dependencies and create an instance of TradingService
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository);

        // Create a user and a trading ID
        User user = new User("userId", "username", "password");
        String tradingId = "tradeId";

        // Configure mock behavior to indicate unauthorized user
        when(mockedTradingRepository.isUserTrade(user.getId(), tradingId)).thenReturn(false);

        // Assert that an HttpStatusException is thrown with specific details
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.deleteTrade(user, tradingId)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("The deal is not owned by the user.", exception.getMessage());
    }

    @Test
    void tradeShouldThrowExceptionForInvalidTradeConditions() {
        // Mock dependencies and create an instance of TradingService
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository);

        // Create a mock requester, trading ID, offered card ID, and a trade request
        User requester = new User("requesterId", "requesterUsername", "password");
        String tradingId = "tradeId";
        String offeredCardId = "offeredCardId";
        TradeRequest trade = new TradeRequest(tradingId, "cardId", "type", 10);

        // Configure mock behaviors for request authentication, trade retrieval, card checks, and card retrieval
        when(mockedTradingRepository.getTradeById(tradingId)).thenReturn(Optional.of(trade));
        when(mockedCardRepository.isCardInStack(requester.getId(), offeredCardId)).thenReturn(true);
        when(mockedCardRepository.isCardInDeck(requester.getId(), offeredCardId)).thenReturn(false);
        when(mockedCardRepository.findCardById(offeredCardId)).thenReturn(Optional.of(new Card("offeredCardId", "CardName", 5, "element", "type")));

        // Assert that an HttpStatusException is thrown with specific details
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.trade(requester, tradingId, offeredCardId)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("The offered card does not meet the trade requirements or is locked in the deck.", exception.getMessage());
    }

}
