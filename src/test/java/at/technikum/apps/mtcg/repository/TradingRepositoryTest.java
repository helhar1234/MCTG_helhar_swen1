package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.repository.trading.TradingRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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


}
