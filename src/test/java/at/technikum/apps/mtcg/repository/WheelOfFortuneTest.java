package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;
import at.technikum.apps.mtcg.repository.wheel.WheelOfFortuneRepository_db;
import at.technikum.server.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Executable;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
public class WheelOfFortuneTest {
    @Test
    void shouldReturnTrueWhenUserHasAlreadySpun() throws SQLException {
        // Mock dependencies
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Setup the behavior of the mocks
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getInt(1)).thenReturn(1); // Indicates the user has spun

        // Create an instance of the repository
        WheelOfFortuneRepository_db repository = new WheelOfFortuneRepository_db(mockedDatabase);

        // Call the method and assert the result
        assertTrue(repository.hasUserSpun("userId"));
    }

    @Test
    void shouldSaveSpinSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedSaveSpinStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedSaveSpinStatement);
        when(mockedSaveSpinStatement.executeUpdate()).thenReturn(1); // Simulates a successful save

        WheelOfFortuneRepository_db repository = new WheelOfFortuneRepository_db(mockedDatabase);

        assertTrue(repository.saveSpin("userId"));
        verify(mockedConnection, times(1)).commit(); // Verify that the transaction is committed
    }

    @Test
    void shouldThrowExceptionOnDatabaseConnectionError() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        when(mockedDatabase.getConnection()).thenThrow(new SQLException("Connection error"));

        WheelOfFortuneRepository_db repository = new WheelOfFortuneRepository_db(mockedDatabase);

        // Assert that the correct exception is thrown
        HttpStatusException thrown = assertThrows(HttpStatusException.class,
                () -> repository.hasUserSpun("userId"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
        assertEquals("Database connection error: java.sql.SQLException: Connection error", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionOnDatabaseQueryError() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenThrow(new SQLException("Query error"));

        WheelOfFortuneRepository_db repository = new WheelOfFortuneRepository_db(mockedDatabase);

        // Assert that the correct exception is thrown
        HttpStatusException thrown = assertThrows(HttpStatusException.class,
                () -> repository.hasUserSpun("userId"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
        assertEquals("Error checking if user has spun: java.sql.SQLException: Query error", thrown.getMessage());
    }


}
