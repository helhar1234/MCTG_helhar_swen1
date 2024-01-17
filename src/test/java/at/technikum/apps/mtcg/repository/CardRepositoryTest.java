package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CardRepositoryTest {
    @Test
    void shouldThrowExceptionOnDatabaseConnectionError() throws SQLException {
        // Create a mock database that throws an SQLException when getConnection is called
        Database mockedDatabase = mock(Database.class);
        when(mockedDatabase.getConnection()).thenThrow(new SQLException("Connection error"));

        // Create an instance of the CardRepository_db class to be tested
        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);

        // Assert that calling findCardById in the presence of a database connection error throws an exception
        assertThrows(HttpStatusException.class, () -> cardRepository.findCardById("cardId"));
    }


    @Test
    void usersShouldSuccessfullyExchangeCards() throws SQLException {
        // Define SQL statements for adding and deleting cards from the user's stack
        String ADD_CARD_TO_STACK_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";
        String DELETE_CARD_FROM_STACK_SQL = "DELETE FROM user_cards WHERE user_fk = ? AND card_fk = ?";

        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedAddCardStmt = mock(PreparedStatement.class);
        PreparedStatement mockedDeleteCardStmt = mock(PreparedStatement.class);

        // Configure mock objects to return success when executeUpdate is called
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(ADD_CARD_TO_STACK_SQL)).thenReturn(mockedAddCardStmt);
        when(mockedConnection.prepareStatement(DELETE_CARD_FROM_STACK_SQL)).thenReturn(mockedDeleteCardStmt);
        when(mockedAddCardStmt.executeUpdate()).thenReturn(1);
        when(mockedDeleteCardStmt.executeUpdate()).thenReturn(1);

        // Create an instance of the CardRepository_db class to be tested
        CardRepository_db cardRepository = new CardRepository_db(mockedDatabase);

        // Define users and cards involved in the exchange
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


}

