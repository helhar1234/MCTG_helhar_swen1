package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

import java.util.Optional;
// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public class TradingService {
    private final TradingRepository tradingRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public TradingService(TradingRepository tradingRepository, CardRepository cardRepository, UserRepository userRepository, SessionService sessionService) {
        this.tradingRepository = tradingRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public Optional<TradeRequest> getTradeById(String id) {
        return tradingRepository.getTradeById(id);
    }

    public boolean createTrade(User user, TradeRequest tradeRequest) {

        if (!cardRepository.isCardInStack(user.getId(), tradeRequest.getCardToTrade()) || cardRepository.isCardInDeck(tradeRequest.getCardToTrade(), user.getId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The deal contains a card that is not owned by the user or locked in the deck.");
        }

        if (tradingRepository.getTradeById(tradeRequest.getId()).isPresent()) {
            throw new HttpStatusException(HttpStatus.CONFLICT, "Conflict: A deal with this deal ID already exists.");
        }
        return tradingRepository.createTrade(tradeRequest, user.getId());
    }

    public TradeRequest[] getAllTrades(User user) {
        TradeRequest[] trades = tradingRepository.getAllTrades();
        if (trades == null || trades.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "No Trading Deals");
        }
        for (TradeRequest trade : trades) {
            // Enrich with card details
            Optional<Card> cardOptional = cardRepository.findCardById(trade.getCardToTrade());
            cardOptional.ifPresent(card -> {
                trade.setCardName(card.getName());
                trade.setCardDamage(card.getDamage());
            });

            // Enrich with user details
            Optional<User> userOptional = userRepository.findUserById(trade.getUserId());
            userOptional.ifPresent(trader -> trade.setUsername(trader.getUsername()));
        }

        return trades;
    }

    public boolean isUserTrade(String userId, String tradingId) {
        return tradingRepository.isUserTrade(userId, tradingId);
    }

    public boolean deleteTrade(User user, String tradingId) {
        if (!isUserTrade(user.getId(), tradingId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The deal is not owned by the user.");
        } else if (getTradeById(tradingId).isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Deal ID Not Found.");
        }
        return tradingRepository.deleteTrade(tradingId);
    }

    public boolean meetsRequirements(TradeRequest tradeRequest, String offeredCardId) {
        Optional<Card> cardOptional = cardRepository.findCardById(offeredCardId);

        if (cardOptional.isPresent()) {
            Card card = cardOptional.get();
            boolean typeMatches = tradeRequest.getType().equals(card.getCardType());
            boolean damageMeets = tradeRequest.getMinimumDamage() <= card.getDamage();
            return typeMatches && damageMeets;
        } else {
            return false;
        }
    }

    public boolean trade(Request request, String tradingId, String offeredCardId) {
        User requester = sessionService.authenticateRequest(request);
        Optional<TradeRequest> trade = getTradeById(tradingId);
        if (trade.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Trading deal not found");
        } else if (requester.getId().equals(trade.get().getUserId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "User cannot trade with themselves");
        }

        if (!cardRepository.isCardInStack(requester.getId(), offeredCardId) || cardRepository.isCardInDeck(requester.getId(), offeredCardId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The offered card is not owned by the user or is locked in the deck");
        }

        if (!meetsRequirements(trade.get(), offeredCardId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The offered card does not meet the trade requirements or is locked in the deck.");
        }

        return cardRepository.deleteCardFromStack(requester.getId(), offeredCardId) &&
                cardRepository.addCardToStack(trade.get().getUserId(), offeredCardId) &&
                cardRepository.deleteCardFromStack(trade.get().getUserId(), trade.get().getCardToTrade()) &&
                cardRepository.addCardToStack(requester.getId(), offeredCardId) &&
                tradingRepository.deleteTrade(tradingId);
    }
}
