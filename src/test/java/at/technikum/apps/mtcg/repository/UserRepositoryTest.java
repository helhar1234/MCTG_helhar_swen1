package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserRepositoryTest {
    @Test
    void shouldReturnTrueWhenUsernameExists() throws SQLException {
        // Mock the database and its related objects
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Define the behavior of the database mock
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);

        // Simulate a database response that finds a matching username
        when(mockedResultSet.next()).thenReturn(true); // Simulates moving to the first row of the result set
        when(mockedResultSet.getInt("count")).thenReturn(1); // Simulates finding one entry matching the username

        // Create an instance of the class under test, using the mocked database
        UserRepository_db userRepository = new UserRepository_db(mockedDatabase);

        // Call the method under test
        boolean exists = userRepository.isUsernameExists("testUserRepository");

        // Assertions and verifications
        assertTrue(exists); // Assert that the method returns true when username is found
        verify(mockedStatement).setString(1, "testUserRepository"); // Verify that the statement was set with the correct username
        verify(mockedResultSet).getInt("count"); // Verify that the count was retrieved from the result set
    }


    @Test
    void shouldSaveUserAndReturnUserWhenSuccessful() throws SQLException {
        // Mock the database and its related objects
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedCreateUserStatement = mock(PreparedStatement.class);
        ResultSet mockedGeneratedKeys = mock(ResultSet.class);
        PreparedStatement mockedSaveUserDataStatement = mock(PreparedStatement.class);

        // Define the behavior of the database mock
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockedCreateUserStatement);
        when(mockedCreateUserStatement.executeUpdate()).thenReturn(1); // Simulates one row affected by the insert
        when(mockedCreateUserStatement.getGeneratedKeys()).thenReturn(mockedGeneratedKeys);
        when(mockedGeneratedKeys.next()).thenReturn(true); // Simulates that a new key was generated
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedSaveUserDataStatement);
        when(mockedSaveUserDataStatement.executeUpdate()).thenReturn(1); // Simulates successful insertion of user data

        // Create an instance of the class under test, using the mocked database
        UserRepository_db userRepository = new UserRepository_db(mockedDatabase);

        // Create a test user
        User testUser = new User("testId", "testUserRepository", "testPassword", 5, 100, false);

        // Call the method under test
        Optional<User> savedUser = userRepository.saveUser(testUser);

        // Assertions and verifications
        assertTrue(savedUser.isPresent()); // Assert that a user is returned
        assertEquals(testUser, savedUser.get()); // Assert that the returned user matches the test user
        verify(mockedConnection, times(1)).setAutoCommit(false); // Verify that the transaction was started
        verify(mockedConnection, times(1)).commit(); // Verify that the transaction was committed
    }


}
