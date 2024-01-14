package at.technikum.apps.mtcg.repository.trading;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradingRepository_db implements TradingRepository {
    // DB CONNECTION
    private final Database database;

    public TradingRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String FIND_TRADE_BY_ID_SQL = "SELECT * from trades WHERE trade_id = ?";
    private final String SAVE_TRADE_SQL = "INSERT INTO trades (trade_id, user_fk, card_fk, expectedType, expectedDamage) VALUES (?,?,?,?,?)";
    private final String SHOW_ALL_TRADES_SQL = "SELECT * FROM trades";
    private final String CHECK_TRADE_OF_USER_SQL = "SELECT COUNT(*) FROM trades WHERE trade_id = ? AND user_fk = ?";
    private final String DELETE_TRADE_SQL = "DELETE FROM trades WHERE trade_id = ?";

    // IMPLEMENTATIONS

    /**
     * Retrieves a trade request by its unique identifier from the database.
     *
     * @param id The unique identifier of the trade request.
     * @return An Optional containing the retrieved trade request, or empty if not found.
     * @throws HttpStatusException If there is an error during retrieval or a database connection issue.
     */
    @Override
    public Optional<TradeRequest> getTradeById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TRADE_BY_ID_SQL)) {

            statement.setString(1, id); // Set the trade request ID as the parameter

            // Execute the query and process the result set
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a TradeRequest object
                    TradeRequest trade = convertResultSetToTradeRequest(resultSet);
                    return Optional.of(trade);
                }
            } catch (SQLException e) {
                System.out.println("Error executing getTradeById: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing getTradeById: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if the trade request is not found or if an exception occurs
    }


    /**
     * Creates a new trade request in the database.
     *
     * @param tradeRequest The trade request to be created.
     * @param userId       The unique identifier of the user creating the trade request.
     * @return True if the trade request is successfully created, false otherwise.
     * @throws HttpStatusException If there is an error during creation or a database connection issue.
     */
    @Override
    public boolean createTrade(TradeRequest tradeRequest, String userId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start a transaction

            try (PreparedStatement createTradeStatement = connection.prepareStatement(SAVE_TRADE_SQL)) {
                createTradeStatement.setString(1, tradeRequest.getId());
                createTradeStatement.setString(2, userId);
                createTradeStatement.setString(3, tradeRequest.getCardToTrade());
                createTradeStatement.setString(4, tradeRequest.getType());
                createTradeStatement.setInt(5, tradeRequest.getMinimumDamage());

                int affectedRows = createTradeStatement.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during trade creation: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during trade creation: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Retrieves all trade requests from the database.
     *
     * @return An array of TradeRequest objects containing all trade requests.
     * @throws HttpStatusException If there is an error during retrieval or a database connection issue.
     */
    @Override
    public TradeRequest[] getAllTrades() {
        List<TradeRequest> trades = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SHOW_ALL_TRADES_SQL)) {

            // Execute the query and process the result set
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    TradeRequest trade = convertResultSetToTradeRequest(resultSet);
                    trades.add(trade);
                }
            } catch (SQLException e) {
                System.out.println("Error during getting all trades: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during getting all trades: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }

        return trades.toArray(new TradeRequest[0]);
    }


    /**
     * Checks if a trade request belongs to a specific user.
     *
     * @param userId    The unique identifier of the user.
     * @param tradingId The unique identifier of the trade request.
     * @return True if the trade request belongs to the user, false otherwise.
     * @throws HttpStatusException If there is an error during the check or a database connection issue.
     */
    @Override
    public boolean isUserTrade(String userId, String tradingId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_TRADE_OF_USER_SQL)) {

            stmt.setString(1, tradingId);
            stmt.setString(2, userId);

            // Execute the query and check the count
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the trade request belongs to the user
                    return resultSet.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.out.println("Error checking if trade is of user: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if trade is of user: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if an error occurs or if the trade request doesn't belong to the user
    }


    /**
     * Deletes a trade request from the database.
     *
     * @param tradingId The unique identifier of the trade request to be deleted.
     * @return True if the trade request is successfully deleted, false otherwise.
     * @throws HttpStatusException If there is an error during deletion or a database connection issue.
     */
    @Override
    public boolean deleteTrade(String tradingId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start a transaction

            try (PreparedStatement deleteTradeStatement = connection.prepareStatement(DELETE_TRADE_SQL)) {
                deleteTradeStatement.setString(1, tradingId); // Set the trade request ID as the parameter

                int affectedRows = deleteTradeStatement.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during trade deletion: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during trade deletion: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
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
