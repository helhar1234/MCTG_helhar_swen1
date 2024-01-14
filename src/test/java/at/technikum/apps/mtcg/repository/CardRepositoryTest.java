package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CardRepositoryTest {

    @Test
    void saveCardShouldReturnTrueWhenCardIsSavedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);
        PackageCard testCard = new PackageCard("cardId", "FireGoblin", 10);

        boolean result = cardRepository.saveCard(testCard);

        assertTrue(result);
        verify(mockedPreparedStatement).executeUpdate();
    }

    @Test
    void findCardByIdShouldReturnCardWhenFound() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        // Assume convertResultSetToCard is a valid method to convert ResultSet to a Card object
        when(mockedResultSet.getString("card_id")).thenReturn("cardId");

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);

        Optional<Card> result = cardRepository.findCardById("cardId");

        assertTrue(result.isPresent());
        assertEquals("cardId", result.get().getId());
    }

    @Test
    void shouldThrowExceptionOnDatabaseConnectionError() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        when(mockedDatabase.getConnection()).thenThrow(new SQLException("Connection error"));

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);

        assertThrows(HttpStatusException.class,
                () -> cardRepository.findCardById("cardId"));
    }

    @Test
    void addCardToDeckShouldReturnTrueWhenSuccessful() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);
        boolean result = cardRepository.addCardToDeck("userId", "cardId");

        assertTrue(result);
        verify(mockedPreparedStatement).executeUpdate();
    }

    @Test
    void usersShouldSuccessfullyExchangeCards() throws SQLException {
        String ADD_CARD_TO_STACK_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";
        String DELETE_CARD_FROM_STACK_SQL = "DELETE FROM user_cards WHERE user_fk = ? AND card_fk = ?";

        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedAddCardStmt = mock(PreparedStatement.class);
        PreparedStatement mockedDeleteCardStmt = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(ADD_CARD_TO_STACK_SQL)).thenReturn(mockedAddCardStmt);
        when(mockedConnection.prepareStatement(DELETE_CARD_FROM_STACK_SQL)).thenReturn(mockedDeleteCardStmt);
        when(mockedAddCardStmt.executeUpdate()).thenReturn(1);
        when(mockedDeleteCardStmt.executeUpdate()).thenReturn(1);

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);

        String user1 = "user1Id";
        String user2 = "user2Id";
        String card1 = "card1Id";
        String card2 = "card2Id";

        // Simulate user1 giving card1 to user2
        cardRepository.deleteCardFromStack(user1, card1);
        cardRepository.addCardToStack(user2, card1);

        // Simulate user2 giving card2 to user1
        cardRepository.deleteCardFromStack(user2, card2);
        cardRepository.addCardToStack(user1, card2);

        // Verify interactions
        verify(mockedDeleteCardStmt, times(2)).executeUpdate(); // Two deletions should have occurred
        verify(mockedAddCardStmt, times(2)).executeUpdate(); // Two additions should have occurred
        verify(mockedDeleteCardStmt).setString(1, user1);
        verify(mockedDeleteCardStmt).setString(2, card1);
        verify(mockedAddCardStmt).setString(1, user2);
        verify(mockedAddCardStmt).setString(2, card1);
        verify(mockedDeleteCardStmt).setString(1, user2);
        verify(mockedDeleteCardStmt).setString(2, card2);
        verify(mockedAddCardStmt).setString(1, user1);
        verify(mockedAddCardStmt).setString(2, card2);
    }

    @Test
    void resetDeckShouldReturnTrueWhenSuccessful() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);
        boolean result = cardRepository.resetDeck("userId");

        assertTrue(result);
        verify(mockedPreparedStatement).executeUpdate();
    }


}

