package at.technikum.apps.mtcg.repository.trading;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.TradeRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradingRepository_db implements TradingRepository {
    // DB CONNECTION
    private final Database database = new Database();

    // SQL STATEMENTS
    private final String FIND_TRADE_BY_ID_SQL = "SELECT * from trades WHERE trade_id = ?";
    private final String SAVE_TRADE_SQL = "INSERT INTO trades (trade_id, user_fk, card_fk, expectedType, expectedDamage) VALUES (?,?,?,?,?)";
    private final String SHOW_ALL_TRADES_SQL = "SELECT * FROM trades";
    private final String CHECK_TRADE_OF_USER_SQL = "SELECT COUNT(*) FROM trades WHERE trade_id = ? AND user_fk = ?";
    private final String DELETE_TRADE_SQL = "DELETE FROM trades WHERE trade_id = ?";

    // IMPLEMENTATIONS
    @Override
    public Optional<TradeRequest> getTradeById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TRADE_BY_ID_SQL)) {

            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a User object
                    TradeRequest trade = convertResultSetToTradeRequest(resultSet);
                    return Optional.of(trade);
                }
            } catch (SQLException e) {
                System.out.println("Error executing getTradeById: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean createTrade(TradeRequest tradeRequest, String userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement createTradeStatement = connection.prepareStatement(SAVE_TRADE_SQL)) {

            createTradeStatement.setString(1, tradeRequest.getId());
            createTradeStatement.setString(2, userId);
            createTradeStatement.setString(3, tradeRequest.getCardToTrade());
            createTradeStatement.setString(4, tradeRequest.getType());
            createTradeStatement.setInt(5, tradeRequest.getMinimumDamage());

            int affectedRows = createTradeStatement.executeUpdate();

            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Error during trade creation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public TradeRequest[] getAllTrades() {
        List<TradeRequest> trades = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SHOW_ALL_TRADES_SQL)) {

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    TradeRequest trade = convertResultSetToTradeRequest(resultSet);
                    trades.add(trade);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving all trades: " + e.getMessage());
        }

        return trades.toArray(new TradeRequest[0]);
    }

    @Override
    public boolean isUserTrade(String userId, String tradingId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_TRADE_OF_USER_SQL)) {

            stmt.setString(1, tradingId);
            stmt.setString(2, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the card is in the stack
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking if trade is of user: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
        }
        return false;
    }

    @Override
    public boolean deleteTrade(String tradingId) {
        try (Connection connection = database.getConnection();
             PreparedStatement createTradeStatement = connection.prepareStatement(DELETE_TRADE_SQL)) {

            createTradeStatement.setString(1, tradingId);

            int affectedRows = createTradeStatement.executeUpdate();

            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Error during trade deletion: " + e.getMessage());
            return false;
        }
    }

    private TradeRequest convertResultSetToTradeRequest(ResultSet resultSet) throws SQLException {
        TradeRequest trade = new TradeRequest();
        trade.setId(resultSet.getString("trade_id"));
        trade.setUserId(resultSet.getString("user_fk"));
        trade.setCardToTrade(resultSet.getString("card_fk"));
        trade.setType(resultSet.getString("expectedType"));
        trade.setMinimumDamage(resultSet.getInt("expectedDamage"));
        return trade;
    }
}