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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionRepositoryTest {

    @Test
    void generateTokenShouldReturnTokenWhenSuccessful() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("token_name")).thenReturn("user-mtcgToken");

        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);
        User user = new User("userId", "user", "password");

        Optional<String> generatedToken = sessionRepository.generateToken(user);

        assertTrue(generatedToken.isPresent());
        assertEquals("user-mtcgToken", generatedToken.get());
    }

    @Test
    void findByTokenShouldReturnUserWhenTokenIsValid() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);

        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("user_id")).thenReturn("userId");

        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);

        Optional<User> user = sessionRepository.findByToken("validToken");

        assertTrue(user.isPresent());
        assertEquals("userId", user.get().getId());
    }

    @Test
    void deleteTokenShouldReturnTrueWhenSuccessful() throws SQLException {
        // Setup mocks
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedDeleteStmt = mock(PreparedStatement.class);
        String DELETE_TOKEN_SQL = "DELETE FROM access_token WHERE user_fk = ?";

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(DELETE_TOKEN_SQL)).thenReturn(mockedDeleteStmt);
        when(mockedDeleteStmt.executeUpdate()).thenReturn(1); // Indicate successful deletion

        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);

        boolean deletionResult = sessionRepository.deleteToken("userId");

        assertTrue(deletionResult);
        verify(mockedDeleteStmt).setString(1, "userId");
    }

    @Test
    void authenticateTokenShouldReturnTrueForValidToken() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedAuthStmt = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedAuthStmt);
        when(mockedAuthStmt.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true); // Token is valid and not expired

        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);

        boolean authResult = sessionRepository.authenticateToken("validToken");

        assertTrue(authResult);
        verify(mockedAuthStmt).setString(1, "validToken");
    }

    @Test
    void authenticateTokenShouldReturnFalseForInvalidToken() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedAuthStmt = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedAuthStmt);
        when(mockedAuthStmt.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(false); // Token is invalid or expired

        SessionRepository_db sessionRepository = new SessionRepository_db(mockedDatabase);

        boolean authResult = sessionRepository.authenticateToken("invalidToken");

        assertFalse(authResult);
        verify(mockedAuthStmt).setString(1, "invalidToken");
    }
}