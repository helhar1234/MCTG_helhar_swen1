package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SessionRepositoryTest {

    @Test
    void generateTokenShouldReturnTokenWhenSuccessful() throws SQLException {
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
        when(mockedResultSet.getString("token_name")).thenReturn("user-mtcgToken");

        // Create an instance of the SessionRepository_db class to be tested
        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);
        User user = new User("userId", "user", "password");

        // Call the generateToken method and assert that it returns the expected token
        Optional<String> generatedToken = sessionRepository.generateToken(user);

        assertTrue(generatedToken.isPresent());
        assertEquals("user-mtcgToken", generatedToken.get());
    }
}