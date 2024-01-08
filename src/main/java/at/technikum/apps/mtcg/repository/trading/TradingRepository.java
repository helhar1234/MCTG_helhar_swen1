package at.technikum.apps.mtcg.repository.trading;

import at.technikum.apps.mtcg.entity.TradeRequest;

import java.sql.SQLException;
import java.util.Optional;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public interface TradingRepository {
    Optional<TradeRequest> getTradeById(String id) throws SQLException;

    boolean createTrade(TradeRequest tradeRequest, String userId) throws SQLException;

    TradeRequest[] getAllTrades() throws SQLException;

    boolean isUserTrade(String userId, String tradingId) throws SQLException;

    boolean deleteTrade(String tradingId) throws SQLException;
}
