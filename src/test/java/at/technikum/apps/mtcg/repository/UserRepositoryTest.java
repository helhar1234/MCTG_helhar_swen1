package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
public class UserRepositoryTest {
    @Test
    void testUpdateCoinsSuccess() throws SQLException {
        // Arrange
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        UserRepository_db userRepository = new UserRepository_db(mockDatabase);

        // Act
        boolean success = userRepository.updateCoins("userId", 100);

        // Assert
        assertTrue(success);
    }

    @Test
    void testAddCardToStackSuccess() throws SQLException {
        // Arrange
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        UserRepository_db userRepository = new UserRepository_db(mockDatabase);
        Card card = new Card("cardId", "testCard", 10, "water", "spell");

        // Act
        boolean success = userRepository.addCardToStack("userId", card);

        // Assert
        assertTrue(success);
    }
    @Test
    void testIsUsernameExistsExceptionHandling() throws SQLException {
        // Arrange
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.getConnection()).thenThrow(new SQLException("Database error"));

        UserRepository_db userRepository = new UserRepository_db(mockDatabase);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userRepository.isUsernameExists("testUser"));
    }

    @Test
    void testFindByUsernameExceptionHandling() throws SQLException {
        // Arrange
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.getConnection()).thenThrow(new SQLException("Database error"));

        UserRepository_db userRepository = new UserRepository_db(mockDatabase);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userRepository.findByUsername("testUser"));
    }




}
