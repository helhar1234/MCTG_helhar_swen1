package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.packages.PackageRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PackageRepositoryTest {

    @Test
    void savePackageShouldReturnTrueWhenPackageIsSavedSuccessfully() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedSavePackageStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedSavePackageStatement);
        when(mockedSavePackageStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("package_id")).thenReturn("packageId");

        // Create an instance of the PackageRepository_db class to be tested
        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);

        // Call the savePackage method and assert that it returns true
        boolean result = packageRepository.savePackage("packageId");

        assertTrue(result); // Assertion: Ensure that the package is saved successfully
    }


    @Test
    void addCardToPackageShouldReturnTrueWhenCardIsAddedSuccessfully() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedConnectStmt = mock(PreparedStatement.class);

        // Set up the mock behavior for the Database and Connection
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        // Mock PreparedStatement for the CONNECT_CARDS_PACKAGES_SQL
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedConnectStmt);
        // Mock the executeUpdate behavior to return 1, indicating a row was affected (inserted)
        when(mockedConnectStmt.executeUpdate()).thenReturn(1);

        // Create an instance of the PackageRepository_db class to be tested
        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "packageId";
        String cardId = "cardId";

        // Call the addCardToPackage method and verify the PreparedStatement and execution
        boolean result = packageRepository.addCardToPackage(packageId, cardId);

        // Verify that the PreparedStatement was set correctly and executed
        verify(mockedConnectStmt).setString(1, cardId);
        verify(mockedConnectStmt).setString(2, packageId);
        verify(mockedConnectStmt).executeUpdate();
    }


    @Test
    void getPackageCardsByIdShouldReturnAllCardsInPackage() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedFindCardsStmt = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedFindCardsStmt);
        when(mockedFindCardsStmt.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, false); // Simulate multiple cards in the package

        // Create an instance of the PackageRepository_db class to be tested
        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "package123";

        // Call the getPackageCardsById method and verify the result
        Card[] cards = packageRepository.getPackageCardsById(packageId);

        assertNotNull(cards);
        assertTrue(cards.length > 0);
        // Additional assertions on specific fields of cards can be added if necessary
    }


    @Test
    void deletePackageShouldReturnTrueWhenPackageIsDeletedSuccessfully() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedDeletePackageStmt = mock(PreparedStatement.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedDeletePackageStmt);
        when(mockedDeletePackageStmt.executeUpdate()).thenReturn(1); // Indicate successful deletion

        // Create an instance of the PackageRepository_db class to be tested
        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "package123";

        // Call the deletePackage method and verify the result
        boolean deletionResult = packageRepository.deletePackage(packageId);

        assertTrue(deletionResult);
        // Verify that the PreparedStatement was set correctly and executed
        verify(mockedDeletePackageStmt).setString(1, packageId);
        verify(mockedDeletePackageStmt).executeUpdate();
    }


}
