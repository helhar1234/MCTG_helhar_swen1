package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository_db;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;

import java.sql.SQLException;
import java.util.Optional;
// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public class TradingService {
    private final TradingRepository tradingRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TradingService(TradingRepository tradingRepository, CardRepository cardRepository, UserRepository userRepository) {
        this.tradingRepository = tradingRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public Optional<TradeRequest> getTradeById(String id) {
        return tradingRepository.getTradeById(id);
    }

    public boolean createTrade(TradeRequest tradeRequest, String userId) {
        if (tradingRepository.getTradeById(tradeRequest.getId()).isPresent()) {
            return false;
        }
        return tradingRepository.createTrade(tradeRequest, userId);
    }

    public TradeRequest[] getAllTrades() throws SQLException {
        TradeRequest[] trades = tradingRepository.getAllTrades();

        for (TradeRequest trade : trades) {
            // Enrich with card details
            Optional<Card> cardOptional = cardRepository.findCardById(trade.getCardToTrade());
            cardOptional.ifPresent(card -> {
                trade.setCardName(card.getName());
                trade.setCardDamage(card.getDamage());
            });

            // Enrich with user details
            Optional<User> userOptional = userRepository.findUserById(trade.getUserId());
            userOptional.ifPresent(user -> trade.setUsername(user.getUsername()));
        }

        return trades;
    }

    public boolean isUserTrade(String userId, String tradingId) {
        return tradingRepository.isUserTrade(userId, tradingId);
    }

    public boolean deleteTrade(String tradingId) {
        return tradingRepository.deleteTrade(tradingId);
    }

    public boolean meetsRequirements(TradeRequest tradeRequest, String offeredCardId) throws SQLException {
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

    public boolean trade(String userIdOfBuyer, String cardIdOfBuyer, String userIdOfSeller, String cardIdOfSeller, String tradeId) throws SQLException {
        return cardRepository.deleteCardFromStack(userIdOfBuyer, cardIdOfBuyer) &&
                cardRepository.addCardToStack(userIdOfSeller, cardIdOfBuyer) &&
                cardRepository.deleteCardFromStack(userIdOfSeller, cardIdOfSeller) &&
                cardRepository.addCardToStack(userIdOfBuyer, cardIdOfSeller) &&
                tradingRepository.deleteTrade(tradeId);
    }
}
