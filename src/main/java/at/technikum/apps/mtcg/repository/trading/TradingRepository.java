package at.technikum.apps.mtcg.repository.trading;

import at.technikum.apps.mtcg.entity.TradeRequest;

import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public interface TradingRepository {
    Optional<TradeRequest> getTradeById(String id);

    boolean createTrade(TradeRequest tradeRequest, String userId);

    TradeRequest[] getAllTrades();

    boolean isUserTrade(String userId, String tradingId);

    boolean deleteTrade(String tradingId);
}
