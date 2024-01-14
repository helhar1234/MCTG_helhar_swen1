package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.dto.TradeRequest;
import at.technikum.apps.mtcg.repository.trading.TradingRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TradingRepositoryTest {
    @Test
    void createTradeShouldReturnTrueWhenTradeIsCreatedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate()).thenReturn(1);

        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        TradeRequest tradeRequest = new TradeRequest("tradeId", "cardId", "expectedType", 10);
        String userId = "user123";

        boolean creationResult = tradingRepository.createTrade(tradeRequest, userId);

        assertTrue(creationResult);
        verify(mockedStatement).setString(1, tradeRequest.getId());
        verify(mockedStatement).setString(2, userId);
        verify(mockedStatement).setString(3, tradeRequest.getCardToTrade());
        verify(mockedStatement).setString(4, tradeRequest.getType());
        verify(mockedStatement).setInt(5, tradeRequest.getMinimumDamage());
    }

    @Test
    void getAllTradesShouldReturnAllTrades() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, false); // Simulate multiple trades

        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);

        TradeRequest[] trades = tradingRepository.getAllTrades();

        assertNotNull(trades);
        assertTrue(trades.length > 0);
        // You can add more assertions to verify the details of the TradeRequest objects
    }

    @Test
    void isUserTradeShouldReturnTrueWhenUserOwnsTrade() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getInt(1)).thenReturn(1); // Indicating the trade exists and belongs to the user

        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        String userId = "user123";
        String tradingId = "trade123";

        boolean result = tradingRepository.isUserTrade(userId, tradingId);

        assertTrue(result);
    }

    @Test
    void deleteTradeShouldReturnTrueWhenTradeIsDeletedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        // Set up the mock behavior for Database and Connection
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        // Mock PreparedStatement for the DELETE_TRADE_SQL
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        // Mock the executeUpdate behavior to return 1, indicating a row was affected (deleted)
        when(mockedStatement.executeUpdate()).thenReturn(1);

        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        String tradingId = "trade123";

        // Call the method under test
        boolean deletionResult = tradingRepository.deleteTrade(tradingId);

        // Verify the outcome
        assertTrue(deletionResult, "Trade should be deleted successfully");
        // Verify that the PreparedStatement was set correctly and executed
        verify(mockedStatement).setString(1, tradingId);
        verify(mockedStatement).executeUpdate();
    }
}
