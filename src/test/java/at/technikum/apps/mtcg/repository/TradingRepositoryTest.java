package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.repository.trading.TradingRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TradingRepositoryTest {
    @Test
    void createTradeShouldReturnTrueWhenTradeIsCreatedSuccessfully() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate()).thenReturn(1);

        // Create an instance of the TradingRepository_db class to be tested
        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        TradeRequest tradeRequest = new TradeRequest("tradeId", "cardId", "expectedType", 10);
        String userId = "user123";

        // Call the createTrade method and assert that it returns true
        boolean creationResult = tradingRepository.createTrade(tradeRequest, userId);

        assertTrue(creationResult);
        // Verify that the PreparedStatement was set correctly with the trade request details
        verify(mockedStatement).setString(1, tradeRequest.getId());
        verify(mockedStatement).setString(2, userId);
        verify(mockedStatement).setString(3, tradeRequest.getCardToTrade());
        verify(mockedStatement).setString(4, tradeRequest.getType());
        verify(mockedStatement).setInt(5, tradeRequest.getMinimumDamage());
    }


    @Test
    void getAllTradesShouldReturnAllTrades() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, false); // Simulate multiple trades

        // Create an instance of the TradingRepository_db class to be tested
        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);

        // Call the getAllTrades method and assert that it returns an array of trades
        TradeRequest[] trades = tradingRepository.getAllTrades();

        assertNotNull(trades);
        assertTrue(trades.length > 0);
        // You can add more assertions to verify the details of the TradeRequest objects
    }


    @Test
    void isUserTradeShouldReturnTrueWhenUserOwnsTrade() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getInt(1)).thenReturn(1); // Indicating the trade exists and belongs to the user

        // Create an instance of the TradingRepository_db class to be tested
        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        String userId = "user123";
        String tradingId = "trade123";

        // Call the isUserTrade method and assert that it returns true
        boolean result = tradingRepository.isUserTrade(userId, tradingId);

        assertTrue(result);
    }


    @Test
    void deleteTradeShouldReturnTrueWhenTradeIsDeletedSuccessfully() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate()).thenReturn(1); // Indicate successful deletion

        // Create an instance of the TradingRepository_db class to be tested
        TradingRepository_db tradingRepository = new TradingRepository_db(mockedDatabase);
        String tradingId = "trade123";

        // Call the deleteTrade method and assert that it returns true
        boolean deletionResult = tradingRepository.deleteTrade(tradingId);

        assertTrue(deletionResult);
        // Verify that the PreparedStatement was set correctly with the trade ID and executed
        verify(mockedStatement).setString(1, tradingId);
        verify(mockedStatement).executeUpdate();
    }

}
