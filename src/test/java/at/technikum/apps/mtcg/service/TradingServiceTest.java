package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.TradeRequest;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TradingServiceTest {

    @Test
    void createTradeShouldSucceedWithValidConditions() {
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository, mockedSessionService);

        User user = new User("userId", "username", "password");
        TradeRequest tradeRequest = new TradeRequest("tradeId", "cardId", "type", 10);

        when(mockedCardRepository.isCardInStack(user.getId(), tradeRequest.getCardToTrade())).thenReturn(true);
        when(mockedCardRepository.isCardInDeck(tradeRequest.getCardToTrade(), user.getId())).thenReturn(false);
        when(mockedTradingRepository.getTradeById(tradeRequest.getId())).thenReturn(Optional.empty());
        when(mockedTradingRepository.createTrade(tradeRequest, user.getId())).thenReturn(true);

        boolean result = tradingService.createTrade(user, tradeRequest);

        assertTrue(result);
    }

    @Test
    void getAllTradesShouldThrowExceptionWhenNoTradesExist() {
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository, mockedSessionService);

        User user = new User("userId", "username", "password");
        when(mockedTradingRepository.getAllTrades()).thenReturn(new TradeRequest[0]);

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.getAllTrades(user)
        );

        assertEquals(HttpStatus.OK, exception.getStatus());
        assertEquals("No Trading Deals", exception.getMessage());
    }

    @Test
    void deleteTradeShouldThrowExceptionForUnauthorizedUser() {
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository, mockedSessionService);

        User user = new User("userId", "username", "password");
        String tradingId = "tradeId";

        when(mockedTradingRepository.isUserTrade(user.getId(), tradingId)).thenReturn(false);

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.deleteTrade(user, tradingId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("The deal is not owned by the user.", exception.getMessage());
    }

    @Test
    void tradeShouldThrowExceptionForInvalidTradeConditions() {
        TradingRepository mockedTradingRepository = mock(TradingRepository.class);
        CardRepository mockedCardRepository = mock(CardRepository.class);
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionService mockedSessionService = mock(SessionService.class);
        TradingService tradingService = new TradingService(mockedTradingRepository, mockedCardRepository, mockedUserRepository, mockedSessionService);

        Request request = mock(Request.class);
        User requester = new User("requesterId", "requesterUsername", "password");
        String tradingId = "tradeId";
        String offeredCardId = "offeredCardId";
        TradeRequest trade = new TradeRequest(tradingId, "cardId", "type", 10);

        when(mockedSessionService.authenticateRequest(request)).thenReturn(requester);
        when(mockedTradingRepository.getTradeById(tradingId)).thenReturn(Optional.of(trade));
        when(mockedCardRepository.isCardInStack(requester.getId(), offeredCardId)).thenReturn(true);
        when(mockedCardRepository.isCardInDeck(requester.getId(), offeredCardId)).thenReturn(false);
        when(mockedCardRepository.findCardById(offeredCardId)).thenReturn(Optional.of(new Card("offeredCardId", "CardName", 5, "element", "type")));

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> tradingService.trade(request, tradingId, offeredCardId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("The offered card does not meet the trade requirements or is locked in the deck.", exception.getMessage());
    }
}
